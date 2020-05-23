package com.haizhi.iap.configure.component;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;
import com.google.common.collect.Maps;
import com.haizhi.iap.common.factory.MongoFactory;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.logging.Level;

/**
 * Created by chenbo on 2017/10/10.
 */
@Component
public class BeanComponent {

    @Setter
    @Value("${inner.mongodb.uri}")
    String mongoUri;

    @Setter
    @Value("${inner.mongodb.database}")
    String dbName;

    @Setter
    @Value("${arango.host}")
    String arangoHost;

    @Setter
    @Value("${arango.port}")
    String arangoPort;

    @Setter
    @Value("${arango.db}")
    String arangoDbName;

    @Setter
    @Value("${arango.username}")
    String arangoUserName;

    @Setter
    @Value("${arango.password}")
    String arangoPassWord;

    @Bean
    public RegisterCenter createRegister(){
        return new RegisterCenter(Maps.newConcurrentMap(), Maps.newConcurrentMap());
    }

    @Bean
    public MongoDatabase getMongoDatabase(){
        java.util.logging.Logger.getLogger("org.mongodb.driver").setLevel(Level.SEVERE);
        MongoClient mongoClient = MongoFactory.get(mongoUri);
        return mongoClient.getDatabase(dbName);
    }

    @Bean
    public ArangoDatabase getArangoDatabase(){
//        ArangoDB arangoDB = new ArangoDB.Builder()
//                .host(arangoHost, Integer.parseInt(arangoPort)).build();
        ArangoDB arangoDB = new ArangoDB.Builder()
                .host(arangoHost, Integer.parseInt(arangoPort)).user(arangoUserName).password(arangoPassWord).build();
        return arangoDB.db(arangoDbName);
    }
}
