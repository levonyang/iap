package com.haizhi.iap.proxy.repo;

import com.haizhi.iap.common.utils.LogicDeleteUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import lombok.Setter;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Created by chenbo on 2017/10/20.
 */
@Repository
public class EnterpriseRepo {
    @Setter
    @Autowired
    MongoDatabase mongoDatabase;

    public Document getBasic(String companyName) {

        MongoCollection<Document> collection = mongoDatabase.getCollection("enterprise_data_gov");

        BasicDBObject filter = new BasicDBObject();
        filter.put("company", companyName);
        LogicDeleteUtil.addDeleteFilter(filter);
        MongoCursor<Document> cursor = collection.find(filter)
                .projection(new BasicDBObject("source_url", 0)).iterator();

        if (cursor.hasNext()) {
            return cursor.next();
        } else {
            return null;
        }
    }
}
