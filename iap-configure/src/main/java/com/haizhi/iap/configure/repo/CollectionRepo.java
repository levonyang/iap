package com.haizhi.iap.configure.repo;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.haizhi.iap.common.exception.ServiceAccessException;
import com.haizhi.iap.configure.model.Param;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;


/**
 * @Author dmy
 * @Date 2017/4/27 下午5:29.
 */
@Slf4j
@Repository
public class CollectionRepo {

    @Setter
    @Autowired
    MongoDatabase mongoDatabase;

    public List<Map> getCollectionByNameAndCondition(String collName, String keyFieldName,
                                                     Integer offset, Integer count, String companyName,
                                                     Param param) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(collName);

        BasicDBObject filter = new BasicDBObject();
        if (!(Strings.isNullOrEmpty(companyName) || Strings.isNullOrEmpty(keyFieldName))) {
            filter.append(keyFieldName, companyName);
        }

        BasicDBObject order = createOrderDBObject(param);

        FindIterable<Document> find = collection.find(filter).sort(order);
        if (offset != null && count != null) {
            find.skip(offset).limit(count);
        }
        MongoCursor<Document> cursor = find.iterator();

        List<Map> objList = Lists.newArrayList();
        while (cursor.hasNext()) {
            Document object = cursor.next();
            if (object.get("_id") != null) {
                object.remove("_id");
            }
            objList.add(object);
        }

        return objList;
    }

    public Long countAllByNameAndCondition(String tableName, String keyFieldName, String companyName) {
        MongoCollection<Document> collection = mongoDatabase.getCollection(tableName);
        BasicDBObject filter = new BasicDBObject();
        if (!Strings.isNullOrEmpty(companyName)) {
            filter.append(keyFieldName, companyName);
        }
//        BasicDBObject order = createOrderDBObject(param);
        return collection.count(filter);
    }

    private BasicDBObject createOrderDBObject(Param param) {
        BasicDBObject filter = new BasicDBObject();
        if (param != null && !param.getId().equals(0l)) {
            if (param.getIsOrder() != null && param.getOrderKey() != null) {
                filter.put(param.getOrderFieldName(), (param.getIsDesc() == null || param.getIsDesc().equals(1)) ? -1 : 1);
            }
        }
        return filter;
    }
}
