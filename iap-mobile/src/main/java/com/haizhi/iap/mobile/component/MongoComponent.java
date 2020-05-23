package com.haizhi.iap.mobile.component;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Created by chenbo on 17/6/20.
 */
@Component
public class MongoComponent {
    @Setter
    @Value("${app.mongodb.database}")
    private String dbName;

    @Setter
    @Autowired
    MongoClient mongo;

    @Bean
    public MongoDatabase getDatabase() {
        return mongo.getDatabase(dbName);
    }
}
