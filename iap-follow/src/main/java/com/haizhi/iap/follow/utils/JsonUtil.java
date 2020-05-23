package com.haizhi.iap.follow.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;

/**
 * Created by chenbo on 2017/12/11.
 */
@Slf4j
public class JsonUtil {

    private static ObjectMapper objectMapper = new ObjectMapper();
    static {
        // 忽略json存在的字段,bean中没有的字段
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static String formatJSON(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("序列化json错误", e);
            return "";
        }
    }

    public static <T> T unformatJSON(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (IOException e) {
            log.error("反序列化json错误", e);
            return null;
        }
    }

    public static <T> T unformatJSON(Map<String, Object> json, Class<T> type) {
        return objectMapper.convertValue(json, type);
    }

    public static Object unformatJSON(String json, Class type, Class type2) {
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructParametricType(type, type2));
        } catch (IOException e) {
            log.error("反序列化json错误", e);
            return null;
        }
    }

    /**
     * 泛型反序列化接口
     *
     * @param json
     * @param type     泛型的Collection Type
     * @param type2    elementClasses 元素类
     * @return
     */
    public static Object unformatJSON(String json, Class type, Class... type2) {
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructParametricType(type, type2));
        } catch (IOException e) {
            log.error("反序列化json错误", e);
            return null;
        }
    }

}