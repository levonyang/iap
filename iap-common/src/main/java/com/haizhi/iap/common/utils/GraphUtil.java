package com.haizhi.iap.common.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by haizhi on 2017/11/9.
 */
public class GraphUtil {

    public static Map buildArangoPathForD3(List<Map> arangoPath) {
        Map<String,List> d3Path = new HashMap<>();

        d3Path.put("vertexes",new ArrayList());
        d3Path.put("edges",new ArrayList());

        for (Map<String,List> item : arangoPath) {
            d3Path.get("edges").add(item.get("edges").get(0));
            d3Path.get("vertexes").addAll(item.get("vertices"));
        }

        return d3Path;
    }

}
