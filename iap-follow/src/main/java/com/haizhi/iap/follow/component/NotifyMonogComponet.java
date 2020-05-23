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

/**
* @description 提供monogodb notify_data库操作对象
* @author LewisLouis
* @date 2018/8/31
*/
@Component
public class NotifyMonogComponet {

    @Setter
    //解决没有配置notify_data仍能正常启动的问题
    @Value("${notify.mongodb.database:${app.mongodb.database}}")
    private String dbName;

    @Setter
    @Autowired
   @Qualifier(value = "notifyMongo")
    MongoClient notifyMongo;

    @Lazy
    @Bean(name = "notifyMongoDatabase")
    public MongoDatabase getDatabase() {
        return notifyMongo.getDatabase(dbName);
    }
}
