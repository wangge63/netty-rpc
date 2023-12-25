package priv.wangg.rpc.codec.kryo;

import priv.wangg.rpc.codec.RpcDecoder;

public class KryoDecoder extends RpcDecoder {

    private final Class<?> genericClass;

    public KryoDecoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected Object deserialize(byte[] data) {
        KryoSerializer serializer = new KryoSerializer();
        Object obj = serializer.deserialize(data, genericClass);
        return obj;
    }
}
