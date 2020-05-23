package com.haizhi.iap.search.utils;

import org.bson.Document;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
* @description MongoDB的document类扩展方法
* @author liulu
* @date 2018/12/20
*/
public class MonogoDocumentUtil {

    /**
    * @description 将Document对象转换为Map对象
    * @param sourceDoc
    * @return java.util.Map<java.lang.String,java.lang.Object>
    * @author liulu
    * @date 2018/12/20
    */
    public static Map<String,Object> documentToMap(Document sourceDoc){
        if (null == sourceDoc){
            return new HashMap<>();
        }
        Map<String,Object> resultMap = new HashMap<>();
        Iterator<String> keyIterator = sourceDoc.keySet().iterator();
        while (keyIterator.hasNext()){
            String key = keyIterator.next();
            resultMap.putIfAbsent(key,sourceDoc.get(key));
        }
        return resultMap;
    }

    /**
    * @description 批量将Document对象转换为Map对象
    * @param sourceDocs
    * @return java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
    * @author liulu
    * @date 2018/12/20
    */
    public static List<Map<String,Object>> documentToMap(List<Document> sourceDocs){
        if (CollectionUtils.isEmpty(sourceDocs)){
            return Collections.emptyList();
        }
        List<Map<String,Object>> results = new ArrayList<>();
        sourceDocs.stream().forEach(document -> {
            Map oneDocMap = documentToMap(document);
            if (CollectionUtils.isEmpty(oneDocMap)){
                return;
            }
            results.add(oneDocMap);
        });

        return results;
    }

}
