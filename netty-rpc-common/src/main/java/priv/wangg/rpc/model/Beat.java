package priv.wangg.rpc.model;

import priv.wangg.rpc.model.RpcRequest;

public class Beat {
    public static final int BEAT_INTERVAL = 30;
    public static final int BEAT_TIMEOUT = BEAT_INTERVAL * 3;
    public static final String BEAT_ID = "BEAT_PING_PONG";

    public static RpcRequest BEAT_PING;

    static {
        BEAT_PING = new RpcRequest();
        BEAT_PING.setRequestId(BEAT_ID);
    }
}
