package priv.wangg.rpc.server.registry;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.wangg.rpc.config.SystemConfig;
import priv.wangg.rpc.protocol.RpcProtocol;
import priv.wangg.rpc.protocol.RpcServiceInfo;
import priv.wangg.rpc.util.ServiceUtil;
import priv.wangg.rpc.zookeeper.CuratorClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ServiceRegistry {
    private static final Logger logger = LoggerFactory.getLogger(ServiceRegistry.class);

    private CuratorClient client;
    private List<String> pathList = new ArrayList<>();

    public ServiceRegistry(String registryAddress) {
        this.client = new CuratorClient(registryAddress, 5000);
    }

    public void registrService(String host, int port, Map<String, Object> serviceMap) {
        List<RpcServiceInfo> serviceInfoList = new ArrayList<>();
        for(String key : serviceMap.keySet()) {
            String[] serviceInfo = key.split(ServiceUtil.SERVICE_CONCAT_TOKEN);
            if (serviceInfo.length > 0) {
                RpcServiceInfo rpcServiceInfo = new RpcServiceInfo();
                rpcServiceInfo.setServiceName(serviceInfo[0]);
                if (serviceInfo.length == 2) {
                    rpcServiceInfo.setServiceVersion(serviceInfo[1]);
                } else {
                    rpcServiceInfo.setServiceVersion("");
                }
                logger.info("Register new service: {}", key);
                serviceInfoList.add(rpcServiceInfo);
            } else {
                logger.error("Can not get service name and version");
            }
        }
        try {
            RpcProtocol protocol = new RpcProtocol();
            protocol.setHost(host);
            protocol.setPort(port);
            protocol.setServiceInfoList(serviceInfoList);
            String serviceData = protocol.toJson();
            byte[] bytes = serviceData.getBytes();
            String path = SystemConfig.ZK_DATA_PATH + protocol.hashCode();
            path = client.createEphemeralSequential(path, bytes);
            pathList.add(path);
            logger.info("Register {} new service success, host: {}, port: {}", serviceInfoList.size(), host, port);
        } catch (Exception e) {
            logger.error("Register service fail, exception: {}", e.getMessage());
        }

        client.addConnectionStateListener(new ConnectionStateListener() {
            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                if (newState == ConnectionState.RECONNECTED) {
                    logger.info("Connection state: {}, register service after reconnected", newState);
                    registrService(host, port, serviceMap);
                }
            }
        });
    }

    public void unregisterService() {
        logger.info("Unregister all service");
        try {
            for (String path : pathList) {
                client.delete(path);
            }
        }catch (Exception e) {
            logger.error("unregister service error" + e.getMessage());
        }
    }

}
