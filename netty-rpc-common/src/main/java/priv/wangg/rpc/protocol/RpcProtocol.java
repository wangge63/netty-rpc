package priv.wangg.rpc.protocol;

import priv.wangg.rpc.util.JsonUtil;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class RpcProtocol implements Serializable {
    private static final long serialVersionUID = 3739804305744970652L;

    private String host;
    private int port;
    private List<RpcServiceInfo> serviceInfoList;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public List<RpcServiceInfo> getServiceInfoList() {
        return serviceInfoList;
    }

    public void setServiceInfoList(List<RpcServiceInfo> serviceInfoList) {
        this.serviceInfoList = serviceInfoList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RpcProtocol that = (RpcProtocol) o;
        return port == that.port && host.equals(that.host)
                && serviceInfoList.equals(that.serviceInfoList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, serviceInfoList);
    }

    public String toJson() {
        return JsonUtil.objectToJson(this);
    }


    public String toString() {
        return this.toJson();
    }

    public static RpcProtocol fromJson(String json) {
        return JsonUtil.jsonToObject(json, RpcProtocol.class);

    }
}
