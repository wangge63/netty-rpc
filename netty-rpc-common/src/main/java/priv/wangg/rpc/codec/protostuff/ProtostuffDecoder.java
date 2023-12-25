package priv.wangg.rpc.codec.protostuff;

import priv.wangg.rpc.codec.RpcDecoder;

public class ProtostuffDecoder extends RpcDecoder {

    private final ProtostuffSerializerPool pool = ProtostuffSerializerPool.getInstnce();
    private final Class<?> genericClass;

    public ProtostuffDecoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected Object deserialize(byte[] data) {
        ProtostuffSerializer serializer = pool.borrow();
        Object obj = serializer.deserialize(data, genericClass);
        pool.restore(serializer);
        return obj;
    }
}
