package priv.wangg.rpc.client.route.impl;

import priv.wangg.rpc.client.handler.RpcClientHandler;
import priv.wangg.rpc.client.route.RpcLoadBalance;
import priv.wangg.rpc.protocol.RpcProtocol;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class RpcLoadBalanceRoundRobin extends RpcLoadBalance {
    private final AtomicInteger roundRobin = new AtomicInteger(0);

    public RpcProtocol doRoute(List<RpcProtocol> addressList) {
        int size = addressList.size();
        int index = (roundRobin.getAndAdd(1) + size) % size;
        return addressList.get(index);
    }
    @Override
    public RpcProtocol route(String serviceKey, Map<RpcProtocol, RpcClientHandler> connectedServerNodes) throws Exception {
        Map<String, List<RpcProtocol>> serviceMap = getServiceMap(connectedServerNodes);
        List<RpcProtocol> addressList = serviceMap.get(serviceKey);
        if(addressList != null && addressList.size() > 0) {
            return doRoute(addressList);
        } else {
            throw new Exception("Can not find connextion for service: " + serviceKey);
        }
    }
}
