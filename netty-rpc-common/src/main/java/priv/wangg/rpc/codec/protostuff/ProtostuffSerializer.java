package priv.wangg.rpc.codec.protostuff;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import priv.wangg.rpc.codec.Serializer;

public class ProtostuffSerializer extends Serializer {

    private static final SchemaCache cachedSchema = SchemaCache.getInstance();
    private static final Objenesis objenesis = new ObjenesisStd(true);

    @Override
    public byte[] serialize(Object obj) {
        Class<?> cls = obj.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);

        try{
            Schema schema = cachedSchema.get(cls);
            return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            buffer.clear();
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> cls) {
        Schema<T> schema = (Schema<T>) cachedSchema.get(cls);
        T obj = objenesis.newInstance(cls);
        ProtostuffIOUtil.mergeFrom(data, obj, schema);
        return obj;
    }

}
