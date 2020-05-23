package com.haizhi.iap.search.utils;

import java.util.HashMap;
import java.util.Map;

public class RangeBuilder{
    private Map<String, Object> range;

    public class Ops {
        public static final String GT = "gt";
        public static final String LT = "lt";
        public static final String GTE = "gte";
        public static final String LTE = "lte";
    }
    
    public RangeBuilder(){
        this.range = new HashMap();
    }

    public RangeBuilder gt(Object value) {
        range.put(Ops.GT, value);
        return this;
    }

    public RangeBuilder lt(Object value) {
        range.put(Ops.LT, value);
        return this;
    }

    public RangeBuilder gte(Object value) {
        range.put(Ops.GTE, value);
        return this;
    }

    public RangeBuilder lte(Object value) {
        range.put(Ops.LTE, value);
        return this;
    }

    public Map<String, Object> build(){
        return this.range;
    }
}