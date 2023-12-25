package priv.wangg.rpc.codec;

public abstract class Serializer {

    protected abstract byte[] serialize(Object obj);

    protected abstract <T> T deserialize(byte[] data, Class<T> clazz);

}
