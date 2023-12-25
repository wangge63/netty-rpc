package priv.wangg.rpc.server.core;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import net.sf.cglib.reflect.FastClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.wangg.rpc.model.Beat;
import priv.wangg.rpc.model.RpcRequest;
import priv.wangg.rpc.model.RpcResponse;
import priv.wangg.rpc.util.ServiceUtil;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private static final Logger logger = LoggerFactory.getLogger(RpcServerHandler.class);

    private final Map<String, Object> handlerMap;
    private final ThreadPoolExecutor serverHandlerPool;

    public RpcServerHandler(Map<String, Object> handlerMap, ThreadPoolExecutor serverHandlerPool) {
        this.handlerMap = handlerMap;
        this.serverHandlerPool = serverHandlerPool;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest) throws Exception {
        if (Beat.BEAT_ID.equalsIgnoreCase(rpcRequest.getRequestId())) {
            logger.info("Server read heartbeat ping");
        }

        serverHandlerPool.execute(new Runnable() {
            @Override
            public void run() {
                logger.info("Receive request " + rpcRequest.getRequestId());
                RpcResponse response = new RpcResponse();
                response.setRequestId(rpcRequest.getRequestId());

                try {
                    Object result = handle(rpcRequest);
                    response.setResult(result);
                } catch (Throwable t) {
                    response.setError(t);
                    logger.error("RPC Server handle request error", t);
                }
                channelHandlerContext.writeAndFlush(response).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        logger.info("Send response for request " + rpcRequest.getRequestId());
                    }
                });

            }
        });
    }

    private Object handle(RpcRequest request) throws Throwable {
        String className = request.getClassName();
        String version = request.getVersion();
        String serviceKey = ServiceUtil.markServiceKey(className, version);
        Object serviceBean = handlerMap.get(serviceKey);
        if (serviceBean == null) {
            logger.error("Can not find service implement with interface name: {} and version: {}", className, version);
            return null;
        }

        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Object[] parameters = request.getParameters();
        Class<?>[] parameterTypes = request.getParameterTypes();

        logger.debug(serviceClass.getName());
        logger.debug(methodName);


        for (int i = 0; i < parameterTypes.length; ++i) {
            logger.debug(parameterTypes[i].getName());
        }
        for (int i = 0; i < parameters.length; ++i) {
            logger.debug(parameters[i].toString());
        }

        FastClass serviceFastClass = FastClass.create(serviceClass);
        int methodIndex = serviceFastClass.getIndex(methodName, parameterTypes);
        return serviceFastClass.invoke(methodIndex, serviceBean, parameters);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.warn("Server caught exception: " + cause.getMessage());
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            ctx.channel().close();
            logger.warn("Channel idle in last {} seconds, close it", Beat.BEAT_TIMEOUT);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
