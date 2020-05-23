package com.haizhi.iap.tag.utils;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ElasticSearchConfig {

    public static final Map<String, Object> DEFAULT_SETTINGS = Maps.newHashMap();
    public static final Map<String, Object> DEFAULT_MAPPINGS = Maps.newHashMap();
    public static List<Map<String, Object>> templates = Lists.newLinkedList();

    static {
        DEFAULT_SETTINGS.put("number_of_shards", 10);
        DEFAULT_SETTINGS.put("number_of_replicas", 1);
        templates = buildDefaultStringMapping(templates,"tag_str", "string", "text", "*_s");
        templates = buildDefaultMapping(templates,"tag_int", "integer", "integer", "*_i");
        templates = buildDefaultMapping(templates,"tag_long", "long", "long", "*_l");
        DEFAULT_MAPPINGS.put("dynamic", true);
        DEFAULT_MAPPINGS.put("dynamic_templates", templates);
    }

    public static List<Map<String, Object>> buildDefaultStringMapping(List<Map<String, Object>> templates,String mappingName, String matchType,
                                                                String targetType, String matchPattern) {
        // string, 支持多选
        Map<String, Object> ret = Maps.newHashMap();
        ret.put("match", matchPattern);
        ret.put("match_mapping_type", matchType);
        Map<String, Object> mapping = Maps.newHashMap();
        mapping.put("type", targetType);
        mapping.put("analyzer", "whitespace");
        mapping.put("index", "analyzed");
        mapping.put("store", true);
        ret.put("mapping",mapping);
        Map<String, Object> ret2 = Maps.newHashMap();
        ret2.put(mappingName,ret);
        templates.add(ret2);
        return templates;
    }

    public static List<Map<String, Object>> buildDefaultMapping(List<Map<String, Object>> templates,String mappingName, String matchType,
                                                          String targetType, String matchPattern) {
        Map<String, Object> ret = Maps.newHashMap();
        ret.put("match", matchPattern);
        ret.put("match_mapping_type", matchType);
        Map<String, Object> mapping = Maps.newHashMap();
        mapping.put("type", targetType);
        mapping.put("index", "not_analyzed");
        mapping.put("store", true);
        ret.put("mapping",mapping);
        Map<String, Object> ret2 = Maps.newHashMap();
        ret2.put(mappingName,ret);
        templates.add(ret2);
        return templates;
    }
}
