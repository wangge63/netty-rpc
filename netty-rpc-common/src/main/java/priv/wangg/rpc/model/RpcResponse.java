package priv.wangg.rpc.model;

import java.io.Serializable;

public class RpcResponse implements Serializable {
    private static final long serialVersionUID = -8298229911549909672L;

    private String requestId;
    private Object result;
    private Throwable error;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    public boolean isError() {
        return error != null;
    }

    @Override
    public String toString() {
        return "RpcResponse{" +
                "requestId='" + requestId + '\'' +
                ", result=" + result +
                ", error=" + error +
                '}';
    }

    public static RpcResponse success(String requestId, Object result) {
        RpcResponse response = new RpcResponse();
        response.setRequestId(requestId);
        response.setResult(result);
        return response;

    }
}
