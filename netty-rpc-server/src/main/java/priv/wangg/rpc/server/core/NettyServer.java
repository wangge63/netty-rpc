package priv.wangg.rpc.server.core;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.wangg.rpc.server.registry.ServiceRegistry;
import priv.wangg.rpc.util.ServiceUtil;
import priv.wangg.rpc.util.ThreadPoolUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

public class NettyServer extends Server{
    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    private Thread thread;
    private String serverAddress;
    private ServiceRegistry serviceRegistry;
    private Map<String, Object> serviceMap = new HashMap<>();

    public NettyServer(String serverAddress, String registryAddress) {
        this.serverAddress = serverAddress;
        this.serviceRegistry = new ServiceRegistry(registryAddress);
    }

    public void addService(String interfaceName, String version, Object serviceBean) {
        logger.info("Adding service, interface: {}, version: {}, bean: {}", interfaceName, version, serviceBean);
        String serviceKey = ServiceUtil.markServiceKey(interfaceName, version);
        serviceMap.put(serviceKey, serviceBean);
    }

    @Override
    public void start() throws Exception {
        thread = new Thread(new Runnable() {
            ThreadPoolExecutor threadPoolExecutor = ThreadPoolUtil.createThreadPool(
                    NettyServer.class.getSimpleName(), 16, 32);
            @Override
            public void run() {
                NioEventLoopGroup bossGroup = new NioEventLoopGroup();
                NioEventLoopGroup workerGroup = new NioEventLoopGroup();

                try {
                    ServerBootstrap serverBootstrap = new ServerBootstrap();
                    serverBootstrap.group(bossGroup, workerGroup)
                            .channel(NioServerSocketChannel.class)
                            .childHandler(new RpcServerInitializer(serviceMap, threadPoolExecutor))
                            .option(ChannelOption.SO_BACKLOG, 128)
                            .childOption(ChannelOption.SO_KEEPALIVE, true);

                    String[] array = serverAddress.split(":");
                    String host = array[0];
                    int port = Integer.parseInt(array[1]);
                    ChannelFuture future = serverBootstrap.bind(host, port).sync();

                    if (serviceRegistry != null) {
                        serviceRegistry.registrService(host, port, serviceMap);
                    }

                    logger.info("Server started on port {}", port);
                    future.channel().closeFuture().sync();
                } catch(Exception e) {
                    if (e instanceof InterruptedException) {
                        logger.info("Rpc server remoting server stop");
                    } else {
                        logger.error("Rpc server remoting server error", e);
                    }
                } finally {
                    try {
                        serviceRegistry.unregisterService();
                        workerGroup.shutdownGracefully();
                        bossGroup.shutdownGracefully();
                    } catch (Exception ex) {
                        logger.error(ex.getMessage(), ex);
                    }
                }

            }
        });
        thread.start();
    }

    @Override
    public void stop() throws Exception {
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
    }
}
