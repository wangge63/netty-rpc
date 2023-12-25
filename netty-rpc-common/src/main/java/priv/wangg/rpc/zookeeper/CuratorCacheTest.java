package priv.wangg.rpc.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import priv.wangg.rpc.config.SystemConfig;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CuratorCacheTest {
    public static void main(String[] args) throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .namespace(SystemConfig.ZK_NAMESPACE)
                .connectString("127.0.0.1:2180,,127.0.0.1:2181,127.0.0.1:2182")
                .sessionTimeoutMs(SystemConfig.ZK_SESSION_TIMEOUT)
                .connectionTimeoutMs(SystemConfig.ZK_CONNECTION_TIMEOUT)
                .retryPolicy(new ExponentialBackoffRetry(1000, 10))
                .build();
        client.start();



        CuratorCache cache = CuratorCache.build(client, "/netty-rpc");
        CuratorCacheListener curatorListener = CuratorCacheListener.builder()
                .forCreates(node -> System.out.println(String.format("Node created: [%s]", node)))
                .forChanges((oldNode, node) -> System.out.println(
                        String.format("Node changed. Old: [%s] New: [%s]", oldNode, node)))
                .forDeletes(oldNode ->
                        System.out.println(String.format("Node deleted. Old value: [%s]", oldNode)))
                .forInitialized(() -> System.out.println("Cache initialized"))
                .build();


        cache.listenable().addListener(curatorListener);
        cache.start();
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        in.readLine();

    }
}
