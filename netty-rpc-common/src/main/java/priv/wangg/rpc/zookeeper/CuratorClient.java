package priv.wangg.rpc.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.wangg.rpc.config.SystemConfig;

import java.util.List;

public class CuratorClient {

    Logger logger = LoggerFactory.getLogger(CuratorClient.class);

    private CuratorFramework client;

    public CuratorClient(String connectString, String namespace, int sessionTimeout, int connectTimeout) {
        client = CuratorFrameworkFactory.builder()
                .namespace(namespace)
                .connectString(connectString)
                .sessionTimeoutMs(sessionTimeout)
                .connectionTimeoutMs(connectTimeout)
                .retryPolicy(new ExponentialBackoffRetry(1000, 10))
                .build();
        client.start();
    }

    public CuratorClient(String connectString, int timeout) {
        this(connectString, SystemConfig.ZK_NAMESPACE, timeout, timeout);
    }

    public CuratorClient(String connectString) {
        this(connectString, SystemConfig.ZK_NAMESPACE, SystemConfig.ZK_SESSION_TIMEOUT, SystemConfig.ZK_CONNECTION_TIMEOUT);
    }

    public CuratorFramework getClient() {
        return client;
    }

    public void close() {
        client.close();
    }

    public void addConnectionStateListener(ConnectionStateListener listener) {
        client.getConnectionStateListenable().addListener(listener);
    }

    public String create(String path, byte[] payload) throws Exception {
        return client.create()
                .creatingParentsIfNeeded()
                .forPath(path, payload);
    }

    public String createEphemeral(String path, byte[] payload) throws Exception {
        return client.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL)
                .forPath(path, payload);
    }

    public String createEphemeralSequential(String path, byte[] payload) throws Exception {
        return client.create()
                .creatingParentsIfNeeded()
                .withProtection()
                .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                .forPath(path, payload);
    }

    public void setData(String path, byte[] payload) throws Exception {
        client.setData().forPath(path, payload);
    }

    public void setDataAsync(String path, byte[] payload) throws Exception {
        CuratorListener listener = new CuratorListener() {
            @Override
            public void eventReceived(CuratorFramework client, CuratorEvent event) throws Exception {
                // examine events for details

                logger.info("event: " + event.getName() + " data: " + new String(event.getData()));
            }
        };

        client.setData().inBackground().forPath(path, payload);
    }

    public void setDataAsyncWithCallback(String path, byte[] payload, BackgroundCallback callback) throws Exception {
        client.setData().inBackground(callback).forPath(path, payload);
    }

    public void delete(String path) throws Exception {
        client.delete().forPath(path);
    }

    public void guaranteeDelete(String path) throws Exception {
        client.delete().guaranteed().forPath(path);
    }

    public void watchNode(String path, Watcher watcher) throws Exception {
        client.getData().usingWatcher(watcher).forPath(path);
    }

    public byte[] getData(String path) throws Exception {
        return client.getData().forPath(path);
    }

    public List<String> getChildren(String path) throws Exception {
        return client.getChildren().forPath(path);
    }

    public List<String> watchedGetChildren(String path) throws Exception {
        return client.getChildren().watched().forPath(path);
    }

    public List<String> watchedGetChildren(String path, Watcher watcher) throws Exception {
        return client.getChildren().usingWatcher(watcher).forPath(path);
    }

    public void watchCache(String path, CuratorCacheListener listener) throws Exception {
        CuratorCache cache =  CuratorCache.build(client, path);
        cache.start();
        cache.listenable().addListener(listener);
    }

    public void watchCache(CuratorCache cache, CuratorCacheListener listener) throws Exception {
        cache.start();
        cache.listenable().addListener(listener);
    }
}
