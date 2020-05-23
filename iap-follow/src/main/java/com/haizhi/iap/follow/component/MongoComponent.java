package com.haizhi.iap.follow.component;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class MongoComponent {
    @Setter
    @Value("${app.mongodb.database}")
    private String dbName;

    @Setter
    @Autowired
    @Qualifier(value = "appMongo")
    MongoClient mongo;

    @Lazy
    @Bean(name = "mongoDatabase")
    public MongoDatabase getDatabase() {
        return mongo.getDatabase(dbName);
    }
}
