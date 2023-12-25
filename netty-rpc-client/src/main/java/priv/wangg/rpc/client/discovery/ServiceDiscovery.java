package priv.wangg.rpc.client.discovery;

import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.wangg.rpc.client.connect.ConnectionManager;
import priv.wangg.rpc.config.SystemConfig;
import priv.wangg.rpc.protocol.RpcProtocol;
import priv.wangg.rpc.zookeeper.CuratorClient;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ServiceDiscovery {
    private static final Logger logger = LoggerFactory.getLogger(ServiceDiscovery.class);
    private CuratorClient curatorClient;

    private CuratorCache curatorCache;

    public ServiceDiscovery(String registryAddress) {
        this.curatorClient = new CuratorClient(registryAddress);
        discoveryService();
    }

    private void discoveryService() {
        try {
            logger.info("Get initial service info");
            getServiceAndUpdateServer();
            curatorCache = CuratorCache.build(curatorClient.getClient(), SystemConfig.ZK_REGISTRY_PATH);

            curatorClient.watchCache(curatorCache,
                    CuratorCacheListener.builder()
                            .forCreates(node -> {
                                logger.info("Node created, try to get latest service list" + node);
                                getServiceAndUpdateServer(node, CuratorCacheListener.Type.NODE_CREATED);
                            })
                            .forChanges(((oldNode, node) -> {
                                logger.info("Node changed, try to get latest service list");
                                getServiceAndUpdateServer(node, CuratorCacheListener.Type.NODE_CHANGED);
                            }))
                            .forDeletes(oldNode -> {
                                logger.info("Node deleted, try to get latest service list");
                                getServiceAndUpdateServer(oldNode, CuratorCacheListener.Type.NODE_DELETED);
                            })
                            .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void getServiceAndUpdateServer() {
        try {
            List<String> nodeList = curatorClient.getChildren(SystemConfig.ZK_REGISTRY_PATH);
            List<RpcProtocol> dataList = new ArrayList<>();
            for (String node :
                    nodeList) {
                logger.debug("Service node: " + node);
                byte[] bytes = curatorClient.getData(SystemConfig.ZK_REGISTRY_PATH + "/" + node);
                String json = new String(bytes);
                RpcProtocol rpcProtocol = RpcProtocol.fromJson(json);
                dataList.add(rpcProtocol);
            }
            logger.debug("Service node data: {}", dataList);
            updateConnectedServer(dataList);
        } catch (Exception e) {
            logger.error("Get node exception: " + e.getMessage());
        }
    }

    private void getServiceAndUpdateServer(ChildData childData, CuratorCacheListener.Type type) {
        String path = childData.getPath();
        String data = new String(childData.getData(), StandardCharsets.UTF_8);
        logger.info("Child data updated, path:{},type:{},data:{},", path, type, data);
        RpcProtocol rpcProtocol =  RpcProtocol.fromJson(data);
        updateConnectedServer(rpcProtocol, type);
    }

    private void updateConnectedServer(List<RpcProtocol> dataList) {
        ConnectionManager.getInstance().updateConnectedServer(dataList);
    }

    private void updateConnectedServer(RpcProtocol rpcProtocol, CuratorCacheListener.Type type) {
        ConnectionManager.getInstance().updateConnectedServer(rpcProtocol, type);
    }

    public void stop() {
        this.curatorCache.close();
        this.curatorClient.close();
    }
}
