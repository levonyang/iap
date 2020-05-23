package com.haizhi.iap.search.component;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Created by chenbo on 17/6/20.
 */
@Component
public class MongoComponent {
    @Value("${app.mongodb.database}")
    private String dbName;

    @Autowired
    @Qualifier("appMongo")
    MongoClient mongo;

    @Bean(name = "appMongoDatabase")
    public MongoDatabase getDatabase() {
        return mongo.getDatabase(dbName);
    }
}
