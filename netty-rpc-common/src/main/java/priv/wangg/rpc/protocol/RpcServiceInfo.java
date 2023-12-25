package priv.wangg.rpc.protocol;

import priv.wangg.rpc.util.JsonUtil;

import java.util.Objects;

public class RpcServiceInfo {

    private String serviceName;
    private String serviceVersion;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RpcServiceInfo that = (RpcServiceInfo) o;
        return Objects.equals(serviceName, that.serviceName)
                && Objects.equals(serviceVersion, that.serviceVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceName, serviceVersion);
    }

    @Override
    public String toString() {
        return this.toJson();
    }

    public String toJson() {
        return JsonUtil.objectToJson(this);
    }
}
