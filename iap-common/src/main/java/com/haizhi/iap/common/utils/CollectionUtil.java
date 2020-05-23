package com.haizhi.iap.common.utils;

import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
* @description 集合操作工具类
* @author liulu
* @date 2018/12/24
*/
public class CollectionUtil {

    /**
    * @description 根据key从Map中获取对应的Value值，并将Value转化为String
    * @param key
    * @param sourceMap
    * @return java.lang.String
    * @author liulu
    * @date 2018/12/24
    */
    public static String findMapValue(Object key, Map sourceMap){
        if (CollectionUtils.isEmpty(sourceMap)){
            return "";
        }

       Object value = sourceMap.get(key);
        if (null == value){
            return "";
        }

        return String.valueOf(value);

    }

    /**
    * @description 从列表中根据指定的key和value找到对应的map
    * @param key
    * @param value
    * @param sourceList
    * @return java.util.Map
    * @author liulu
    * @date 2018/12/24
    */
    public static Map findMap(String key, String value, List<Map<String,Object>> sourceList){
        if (CollectionUtils.isEmpty(sourceList)){
            return null;
        }
        Map mapResult =  sourceList.stream().filter(oneMap -> {

            String findValue = findMapValue(key,oneMap);
            if (findValue.equalsIgnoreCase(value)){
                return true;
            }
            return false;

        }).findFirst().orElse(null);

        return mapResult;

    }
}
