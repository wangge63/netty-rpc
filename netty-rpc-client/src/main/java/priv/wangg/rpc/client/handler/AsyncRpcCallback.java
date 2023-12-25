package priv.wangg.rpc.client.handler;

public interface AsyncRpcCallback {

    void success(Object result);

    void fail(Exception e);

}
