package com.haizhi.iap.common.factory;

import com.google.common.base.Strings;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by chenbo on 17/6/16.
 */
@Slf4j
public class MongoFactory {

    public static MongoClient get(String uri) {
        if (Strings.isNullOrEmpty(uri)) {
            throw new IllegalArgumentException("mongodb.uri must be set!");
        } else {
            log.info("mongodb.uri: {}", uri);
        }

        MongoClientURI clientURI = new MongoClientURI(uri);
        MongoClient mongo = new MongoClient(clientURI);
        return mongo;
    }
}