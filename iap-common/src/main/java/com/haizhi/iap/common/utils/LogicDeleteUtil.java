package com.haizhi.iap.common.utils;

import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Filters;
import org.bson.conversions.Bson;

/**
 * Created by chenbo on 2017/12/25.
 */
public class LogicDeleteUtil {

    public static BasicDBObject addDeleteFilter(BasicDBObject filter){
        filter.put("logic_delete", new BasicDBObject("$ne", 1));
        return filter;
    }

    public static Bson addDeleteFileter() {
        return Filters.ne("logic_delete", 1);
    }

}
