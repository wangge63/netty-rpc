package priv.wangg.rpc.codec.protostuff;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import priv.wangg.rpc.SystemConfig;

import java.time.Duration;

public class ProtostuffSerializerPool {

    private GenericObjectPool<ProtostuffSerializer> pool;

    private static volatile ProtostuffSerializerPool poolFactory;

    private ProtostuffSerializerPool() {
        pool = new GenericObjectPool<>(new ProtostuffSerializerFactory());
    }

    public ProtostuffSerializerPool(final int maxTotal, final int minIdle,
                                    final Duration maxWaitMillis, final Duration minEvictableIdleTimeMills) {
        GenericObjectPoolConfig<ProtostuffSerializer> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(maxTotal);
        config.setMinIdle(minIdle);
        config.setMaxWait(maxWaitMillis);
        config.setMinEvictableIdleDuration(minEvictableIdleTimeMills);
        pool = new GenericObjectPool<>(new ProtostuffSerializerFactory());

        pool.setConfig(config);
    }

    public static ProtostuffSerializerPool getInstnce() {
        if (poolFactory == null) {
            synchronized (ProtostuffSerializerPool.class) {
                if (poolFactory == null) {
                    poolFactory = new ProtostuffSerializerPool(SystemConfig.SERIALIZE_POOL_MAX_TOTAL,
                            SystemConfig.SERIALIZE_POOL_MIN_IDLE,
                            SystemConfig.SERIALIZE_POOL_MAX_WAIT_MILLIS,
                            SystemConfig.SERIALIZE_POOL_MIN_EVICTABLE_IDLE_TIME_MILLIS);
                }
            }
        }
        return poolFactory;
    }

    public ProtostuffSerializer borrow() {
        try {
            return getProtostuffSerializerPool().borrowObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void restore(final ProtostuffSerializer object) {
        getProtostuffSerializerPool().returnObject(object);
    }

    public GenericObjectPool<ProtostuffSerializer> getProtostuffSerializerPool() {
        return pool;
    }

}
