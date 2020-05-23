package com.haizhi.iap.common.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
//        Pretty 打印
//        MAPPER.enable(SerializationFeature.INDENT_OUTPUT);

        SimpleModule simpleModule = new SimpleModule();

        simpleModule.addSerializer(LocalDateTime.class, new LocalDateTimeJsonSerializer());

        MAPPER.registerModule(simpleModule);

    }

    public static ObjectMapper getMapper() {
        return MAPPER;
    }

    public static Map<String, Object> ok(Object data) {
        Map<String, Object> ret = new HashMap<String, Object>();
        ret.put("code", 0);
        ret.put("data", data);
        return ret;
    }

    public static Map<String, Object> ok() {
        return ok("操作成功");
    }

    public static Map<String, Object> error(String message) {
        Map<String, Object> ret = new HashMap<String, Object>();
        ret.put("code", -1);
        ret.put("message", message);
        return ret;
    }

    public static <T> Map<String, Object> page(List<T> list, Integer totalCount) {
        Map<String, Object> ret = new HashMap<String, Object>();
        ret.put("code", 0);
        ret.put("data", list);
        ret.put("recordsFiltered", totalCount);
        ret.put("recordsTotal", totalCount);
        return ret;
    }

}
