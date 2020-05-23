package com.haizhi.iap.follow.repo;

import com.google.common.collect.Lists;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import lombok.Setter;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Author dmy
 * @Date 2017/12/19 下午2:18.
 */
@Repository
public class MongoRepo {
    @Setter
    @Autowired
    @Qualifier("mongoDatabase")
    MongoDatabase mongoDatabase;

    public MongoCursor<Document> getById(String collection, String id) {
        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(collection);
        BasicDBObject filter = new BasicDBObject();
        filter.put("_id", new ObjectId(id));

        MongoCursor<Document> cursor = mongoCollection.find(filter).iterator();
        return cursor;
    }

    public MongoCursor<Document> getByIds(String collection, List<String> ids) {
        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(collection);
        List<ObjectId> fids = Lists.newArrayList();
        ids.forEach(one -> fids.add(new ObjectId(one)));

        MongoCursor<Document> cursor = mongoCollection.find(Filters.in("_id", fids)).iterator();
        return cursor;
    }

    public MongoCursor<Document> getByFields(String collection, String field, List<String> values) {
        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(collection);

        BasicDBList queryList = new BasicDBList();
        for(String value : values) {
            BasicDBObject filter = new BasicDBObject();
            filter.put(field, value);
            queryList.add(filter);
        }

        BasicDBObject query = new BasicDBObject();
        query.put("$or", queryList);

        MongoCursor<Document> cursor = mongoCollection.find(query).iterator();
        return cursor;
    }
}
