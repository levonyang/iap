package com.haizhi.iap.search.repo;

import com.haizhi.iap.common.utils.LogicDeleteUtil;
import com.haizhi.iap.search.conf.AppDataCollections;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

/**
 * Created by chenbo on 17/2/15.
 */
@Repository
public class CaiBaoCompaniesAbilityRepo {

    @Qualifier("appMongoDatabase")
    @Autowired
    MongoDatabase appMongoDatabase;

    public Document getBasic(String stockCode,String yearMonth) {

    	Document doc = null;
        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_COMPANY_ABILITY);

        BasicDBObject filter = new BasicDBObject();
        filter.put("code", stockCode);
        filter.put("year_month", yearMonth);
        LogicDeleteUtil.addDeleteFilter(filter);

        MongoCursor<Document> cursor = collection.find(filter).projection(new BasicDBObject("source_url", 0)).iterator();
        if (cursor.hasNext()) {
            doc = cursor.next();
            return doc;
        } else {
            return null;
        }
    }

}
