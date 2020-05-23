package com.haizhi.iap.follow.utils;

import com.mongodb.BasicDBObject;

/**
 * Created by chenbo on 2017/12/25.
 */
public class LogicDeleteUtil {

    public static BasicDBObject addDeleteFilter(BasicDBObject filter){
        filter.put("logic_delete", new BasicDBObject("$ne", 1));
        return filter;
    }

}
