package priv.wangg.rpc.codec.protostuff;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class ProtostuffSerializerFactory extends BasePooledObjectFactory<ProtostuffSerializer> {

    @Override
    public ProtostuffSerializer create() throws Exception {
        return createProtostuffSerializer();
    }

    @Override
    public PooledObject<ProtostuffSerializer> wrap(ProtostuffSerializer protostuffSerializer) {
        return new DefaultPooledObject<>(protostuffSerializer);
    }

    private ProtostuffSerializer createProtostuffSerializer(){
        return new ProtostuffSerializer();
    }
}
