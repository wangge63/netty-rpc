package priv.wangg.rpc.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.text.SimpleDateFormat;
import java.util.HashMap;

public class JsonUtil {
    private static ObjectMapper objMapper = new ObjectMapper();

    static {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss");
        objMapper.setDateFormat(dateFormat);
        objMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objMapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        objMapper.configure(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT, false);
        objMapper.disable(SerializationFeature.FLUSH_AFTER_WRITE_VALUE);
        objMapper.disable(SerializationFeature.CLOSE_CLOSEABLE);
        objMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objMapper.configure(JsonParser.Feature.IGNORE_UNDEFINED, true);
    }

    public static <T> byte[] serialize(T obj) {
        byte[] bytes = new byte[0];
        try {
            bytes = objMapper.writeValueAsBytes(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bytes;
    }

    public static <T> T deserialize(byte[] data, Class<T> cls) {
        T obj = null;
        try {
            obj = objMapper.readValue(data, cls);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    public static <T> T jsonToObject(String json, Class<T> cls) {
        T obj = null;
        try {
            obj = objMapper.readValue(json, cls);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    public static <T> T jsonToObjectHashMap(String json, Class<?> keyClass,
                                                    Class<?> valueClass) {
        T obj = null;
        JavaType javaType = objMapper.getTypeFactory().constructMapType(HashMap.class, keyClass, valueClass);

        try {
            obj = objMapper.readValue(json, javaType);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        return obj;
    }

    public static <T> T jsonToObjectList(String json,
                                         Class<T> collectionClass, Class<?> elementClass) {
        T obj = null;
        JavaType javaType = objMapper.getTypeFactory().constructParametricType(collectionClass, elementClass);

        try {
            obj = objMapper.readValue(json, javaType);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        return obj;
    }

    public static String objectToJson(Object obj) {
        String json = null;
        try {
            json = objMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        return json;
    }
}
