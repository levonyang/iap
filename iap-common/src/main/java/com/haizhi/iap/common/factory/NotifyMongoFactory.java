package com.haizhi.iap.common.factory;


import com.google.common.base.Strings;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import lombok.extern.slf4j.Slf4j;

/**
* @description Mongodb notify_data库的连接工厂
* @author LewisLouis
* @date 2018/8/31
*/
@Slf4j
public class NotifyMongoFactory {

    public static MongoClient get(String uri) {
        if (Strings.isNullOrEmpty(uri)) {
            throw new IllegalArgumentException("notify.mongodb.uri must be set!");
        }

        log.info("notify.mongodb.uri: {}", uri);

        MongoClientURI clientURI = new MongoClientURI(uri);
        MongoClient mongo = new MongoClient(clientURI);
        return mongo;
    }
}
