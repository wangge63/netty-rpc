package priv.wangg.rpc.codec.protostuff;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class SchemaCache {

    private static class SchemaCacheHolder {
        private static final SchemaCache INSTANCE = new SchemaCache();
    }

    public static SchemaCache getInstance() {
        return SchemaCacheHolder.INSTANCE;
    }

    private Cache<Class<?>, Schema<?>> cache = CacheBuilder.newBuilder()
            .maximumSize(1024)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build();

    public Schema<?> get(final Class<?> cls, Cache<Class<?>, Schema<?>> cache) {
        try {
            return cache.get(cls, new Callable<RuntimeSchema<?>>() {
                @Override
                public RuntimeSchema<?> call() throws Exception {
                    return RuntimeSchema.createFrom(cls);
                }
            });
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public Schema<?> get(Class<?> cls) {
        return get(cls, cache);
    }

}
