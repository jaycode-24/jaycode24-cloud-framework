package com.jaycode.framework.cloud.boot.core.support.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

/**
 * JSON处理类
 * 不再建议使用fastjson包，最近爆出的bug太多
 *
 * @author jinlong.wang
 */
public class Json {

    private static final Json DEFAULT = new Json(Feature.SERIALIZE_NON_NULL);

    private final ObjectMapper mapper;


    public enum Feature {
        //只序列化非空字段
        SERIALIZE_NON_NULL
    }

    public Json(Feature... features) {
        mapper = new ObjectMapper();
        //忽略属性不存在的错误
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        for (Feature feature : features) {
            if (feature.equals(Feature.SERIALIZE_NON_NULL)) {
                mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            }
        }
    }

    /**
     * 将对象转换为json字符串
     *
     * @param obj 对象
     * @return json字符串
     */
    public String toJsonString(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new JsonException(e.getMessage(), e);
        }
    }

    public byte[] toJsonBytes(Object obj) {
        try {
            return mapper.writeValueAsBytes(obj);
        } catch (Exception e) {
            throw new JsonException(e.getMessage(), e);
        }
    }


    private <T> T parseObject(String jsonStr, Class<T> cls) {
        try {
            return (T) mapper.readValue(jsonStr, cls);
        } catch (Exception e) {
            throw new JsonException(e.getMessage(), e);
        }
    }

    /**
     * 将对象转换为json字符串
     *
     * @param obj 对象
     * @return json字符串
     */
    public static String toString(Object obj) {
        return DEFAULT.toJsonString(obj);
    }

    public static byte[] toBytes(Object obj) {
        return DEFAULT.toJsonBytes(obj);
    }

    /**
     * 创建json访问器
     *
     * @param jsonStr json字符串
     * @return
     */
    public static Element parse(String jsonStr) {
        return new Element(jsonStr);
    }

    /**
     * 直接将json字符串转换为响应的class
     *
     * @param jsonStr json字符串
     * @param cls     目标类
     * @param <T>     类型
     * @return
     */
    public static <T> T parse(String jsonStr, Class<T> cls) {
        return DEFAULT.parseObject(jsonStr, cls);
    }


    public static class Element {
        private Map<String, Object> map;

        private Element(String jsonStr) {
            map = parse(jsonStr, Map.class);
        }

        public String getString(String key) {
            Object v = map.get(key);
            if (v == null) {
                return null;
            }
            return String.valueOf(v);
        }

        public Map<String, Object> getItem(String key) {
            return (Map) map.get(key);
        }

        public <T> T getItem(String key, Class<T> cls) {
            return Json.parse(Json.toString(map.get(key)), cls);
        }

        public List getItems(String key) {
            return (List) map.get(key);
        }

        public List<Map<String, Object>> getMapItems(String key) {
            return (List) map.get(key);
        }


    }
}
