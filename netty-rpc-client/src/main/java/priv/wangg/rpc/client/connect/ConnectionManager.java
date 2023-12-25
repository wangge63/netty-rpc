package priv.wangg.rpc.client.connect;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.wangg.rpc.client.handler.RpcClientHandler;
import priv.wangg.rpc.client.handler.RpcClientInitializer;
import priv.wangg.rpc.client.route.RpcLoadBalance;
import priv.wangg.rpc.client.route.impl.RpcLoadBalanceRoundRobin;
import priv.wangg.rpc.protocol.RpcProtocol;
import priv.wangg.rpc.protocol.RpcServiceInfo;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConnectionManager {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionManager.class);

    private final EventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);
    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(4, 8,
            600L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(1000));

    private Map<RpcProtocol, RpcClientHandler> connectedServerNodes = new ConcurrentHashMap<>();
    private Set<RpcProtocol> rpcProtocolSet = new CopyOnWriteArraySet<>();
    private final Lock lock = new ReentrantLock();
    private final Condition connected = lock.newCondition();
    private long waitTimeout = 5000;
    private volatile boolean isRunning = true;
    private RpcLoadBalance loadBalance = new RpcLoadBalanceRoundRobin();

    private ConnectionManager() {}

    private static class SingletonHolder {
        private static final ConnectionManager instance = new ConnectionManager();
    }

    public static ConnectionManager getInstance() {
        return SingletonHolder.instance;
    }

    public void updateConnectedServer(List<RpcProtocol> serviceList) {
        if (serviceList != null && serviceList.size() > 0) {
            Set<RpcProtocol> serviceSet = new HashSet<>(serviceList.size());
            for (int i = 0; i < serviceList.size(); i++) {
                RpcProtocol rpcProtocol = serviceList.get(i);
                serviceSet.add(rpcProtocol);
            }
            for (final RpcProtocol rpcProtocol :
                    serviceSet) {
                if (!rpcProtocolSet.contains(rpcProtocol)) {
                    connectServerNode(rpcProtocol);
                }
            }

            for (RpcProtocol rpcProtocol :
                 rpcProtocolSet) {
                if (!serviceSet.contains(rpcProtocol)) {
                    logger.info("Remove invalid service: " + rpcProtocol.toJson());
                    removeAndCloseHandler(rpcProtocol);
                }
            }
        } else {
            logger.error("No available service");
            for (RpcProtocol rpcProtocol :
                    rpcProtocolSet) {
                removeAndCloseHandler(rpcProtocol);
            }
        }
    }

    public void updateConnectedServer(RpcProtocol rpcProtocol, CuratorCacheListener.Type type) {
        if (rpcProtocol == null) {
            return;
        }
        if (type == CuratorCacheListener.Type.NODE_CREATED && !rpcProtocolSet.contains(rpcProtocol)) {
            connectServerNode(rpcProtocol);
        } else if (type == CuratorCacheListener.Type.NODE_CHANGED) {
            //TODO We may don't need to reconnect remote server if the server'IP and server'port are not changed
            removeAndCloseHandler(rpcProtocol);
            connectServerNode(rpcProtocol);
        } else if (type == CuratorCacheListener.Type.NODE_DELETED) {
            removeAndCloseHandler(rpcProtocol);
        } else {
            throw new IllegalArgumentException("Unknow type:" + type);
        }
    }

    private void connectServerNode(RpcProtocol rpcProtocol) {
        if (rpcProtocol.getServiceInfoList() == null || rpcProtocol.getServiceInfoList().size() == 0) {
            logger.info("No service on node, host: {}, port: {}", rpcProtocol.getHost(), rpcProtocol.getPort());
            return;
        }
        rpcProtocolSet.add(rpcProtocol);
        for (RpcServiceInfo serviceInfo :
                rpcProtocol.getServiceInfoList()) {
            logger.info("New service info, name: {}, version: {}", serviceInfo.getServiceName(), serviceInfo.getServiceVersion());
        }
        final InetSocketAddress remotePeer = new InetSocketAddress(rpcProtocol.getHost(), rpcProtocol.getPort());
        threadPoolExecutor.submit(new Runnable() {
            @Override
            public void run() {
                Bootstrap b = new Bootstrap();
                b.group(eventLoopGroup)
                        .channel(NioSocketChannel.class)
                        .handler(new RpcClientInitializer());

                ChannelFuture channelFuture = b.connect(remotePeer);
                channelFuture.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        if (channelFuture.isSuccess()) {
                            logger.info("Successfully connect to remote server, remote peer = " + remotePeer);
                            RpcClientHandler handler = channelFuture.channel().pipeline().get(RpcClientHandler.class);
                            connectedServerNodes.put(rpcProtocol, handler);
                            handler.setRpcProtocol(rpcProtocol);
                            signalAvailableHandler();
                        } else {
                            logger.error("Can not connect to remote server, remote peer = " + remotePeer);
                        }
                    }
                });
            }
        });
    }

    private void signalAvailableHandler() {
        lock.lock();
        try{
            connected.signalAll();
        } finally {
            lock.unlock();
        }
    }

    private boolean waitingForHandler() throws InterruptedException {
        lock.lock();
        try {
            logger.warn("Waiting for available handler");
            return connected.await(this.waitTimeout,  TimeUnit.MILLISECONDS);
        } finally {
            lock.unlock();
        }
    }

    private void removeAndCloseHandler(RpcProtocol rpcProtocol) {
        RpcClientHandler handler = connectedServerNodes.get(rpcProtocol);
        if (handler != null) {
            handler.close();
        }
        connectedServerNodes.remove(rpcProtocol);
        rpcProtocolSet.remove(rpcProtocol);
    }

    public void removeHandler(RpcProtocol rpcProtocol) {
        rpcProtocolSet.remove(rpcProtocol);
        connectedServerNodes.remove(rpcProtocol);
        logger.info("Remove one connection, host: {}, port: {}", rpcProtocol.getHost(), rpcProtocol.getPort());
    }

    public RpcClientHandler chooseHandler(String serviceKey) throws Exception {
        int size = connectedServerNodes.values().size();
        while (isRunning && size <= 0) {
            try {
                waitingForHandler();
                size = connectedServerNodes.values().size();
            } catch (InterruptedException e) {
                logger.error("Waiting for available service is interrupted!", e);
            }
        }
        RpcProtocol rpcProtocol = loadBalance.route(serviceKey, connectedServerNodes);
        RpcClientHandler handler = connectedServerNodes.get(rpcProtocol);
        if (handler != null) {
            return handler;
        } else {
            throw new Exception("Can not get available connection");
        }
    }

    public void stop() {
        isRunning = false;
        for (RpcProtocol rpcProtocol :
                rpcProtocolSet) {
            removeAndCloseHandler(rpcProtocol);
        }
        signalAvailableHandler();
        threadPoolExecutor.shutdown();
        eventLoopGroup.shutdownGracefully();
    }
}
