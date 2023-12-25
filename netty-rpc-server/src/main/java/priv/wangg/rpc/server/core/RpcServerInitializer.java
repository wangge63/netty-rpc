package priv.wangg.rpc.server.core;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import priv.wangg.rpc.codec.Serializer;
import priv.wangg.rpc.codec.protostuff.ProtostuffDecoder;
import priv.wangg.rpc.codec.protostuff.ProtostuffEncoder;
import priv.wangg.rpc.codec.protostuff.ProtostuffSerializer;
import priv.wangg.rpc.model.Beat;
import priv.wangg.rpc.model.RpcRequest;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RpcServerInitializer extends ChannelInitializer<SocketChannel> {

    private Map<String, Object> handlerMap;
    private ThreadPoolExecutor threadPoolExecutor;

    public RpcServerInitializer(Map<String, Object> handlerMap, ThreadPoolExecutor threadPoolExecutor) {
        this.handlerMap = handlerMap;
        this.threadPoolExecutor = threadPoolExecutor;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        Serializer serializer = new ProtostuffSerializer();
        ChannelPipeline pipeline = socketChannel.pipeline();

        pipeline.addLast(new IdleStateHandler(0, 0, Beat.BEAT_TIMEOUT, TimeUnit.SECONDS));
        pipeline.addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0));
        pipeline.addLast(new ProtostuffDecoder(RpcRequest.class));
        pipeline.addLast(new ProtostuffEncoder());
        pipeline.addLast(new RpcServerHandler(handlerMap, threadPoolExecutor));
    }
}
