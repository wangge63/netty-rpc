package priv.wangg.rpc.client.route;

import org.apache.commons.collections4.map.HashedMap;
import priv.wangg.rpc.client.handler.RpcClientHandler;
import priv.wangg.rpc.protocol.RpcProtocol;
import priv.wangg.rpc.protocol.RpcServiceInfo;
import priv.wangg.rpc.util.ServiceUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class RpcLoadBalance {

    protected Map<String, List<RpcProtocol>> getServiceMap(Map<RpcProtocol, RpcClientHandler> connectedServerNodes) {
        Map<String, List<RpcProtocol>> serviceMap = new HashedMap<>();
        if (connectedServerNodes != null && connectedServerNodes.size() > 0) {
            for (RpcProtocol rpcProtocol : connectedServerNodes.keySet()) {
                for (RpcServiceInfo serviceInfo : rpcProtocol.getServiceInfoList()) {
                    String serviceKey = ServiceUtil.markServiceKey(serviceInfo.getServiceName(), serviceInfo.getServiceVersion());
                    List<RpcProtocol> rpcProtocolList = serviceMap.get(serviceKey);
                    if (rpcProtocolList == null) {
                        rpcProtocolList = new ArrayList<>();
                    }
                    rpcProtocolList.add(rpcProtocol);
                    serviceMap.putIfAbsent(serviceKey, rpcProtocolList);
                }
            }
        }
        return serviceMap;
    }

    public abstract RpcProtocol route(String serviceKey, Map<RpcProtocol, RpcClientHandler> connectedServerNodes) throws Exception;
}
