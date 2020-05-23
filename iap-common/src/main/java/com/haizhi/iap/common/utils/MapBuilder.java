package com.haizhi.iap.common.utils;

import java.util.LinkedHashMap;
import java.util.Map;

public class MapBuilder {

    private boolean ignoreValue = true;
    private LinkedHashMap<String, Object> map;

    public MapBuilder() {
        map = new LinkedHashMap<String, Object>();
    }

    public MapBuilder(boolean ignoreValue) {
        this();
        this.ignoreValue = ignoreValue;
    }

    public MapBuilder(String key, Object value) {
        this();
        put(key, value);
    }

    public MapBuilder(String key, Object value, boolean ignoreValue) {
        this();
        this.ignoreValue = ignoreValue;
        put(key, value);
    }

    public MapBuilder put(String key, Object value) {
        return put(key, value, false);
    }

    public MapBuilder put(String key, Object value, boolean toString) {
        if (!this.ignoreValue || (key != null && value != null)) {
            map.put(key, toString ? value.toString() : value);
        }
        return this;
    }

    public Map<String, Object> build() {
        return map;
    }

}
