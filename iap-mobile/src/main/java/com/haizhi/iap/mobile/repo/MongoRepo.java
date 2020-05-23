package com.haizhi.iap.mobile.repo;

import com.haizhi.iap.mobile.bean.normal.MongoQuery;
import com.haizhi.iap.mobile.bean.normal.ToMongoQuery;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CountOptions;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by thomas on 18/4/13.
 */
@Repository
public class MongoRepo
{
    @Autowired
    private MongoDatabase mongoDatabase;

    /**
     * 通用的mongo查询接口
     *
     * @param toMongoQuery
     * @return
     */
    public List<Map<String, Object>> query(ToMongoQuery toMongoQuery) {
        MongoQuery query = null;
        if(toMongoQuery == null || (query = toMongoQuery.toMongoQuery()) == null) return Collections.emptyList();
        MongoCollection<Document> collection = mongoDatabase.getCollection(query.getTable());

        //filter
        FindIterable<Document> findIterable = query.getFilter() == null ? collection.find() : collection.find(query.getFilter());
        //limit
        if(query.getOffset() != null && query.getSize() != null)
            findIterable.skip(query.getOffset()).limit(query.getSize());
        //sort
        if(query.getSort() != null) findIterable.sort(query.getSort());
        //projection
        if(query.getProjection() != null)
            findIterable.projection(query.getProjection());

        List<Map<String, Object>> results = new ArrayList<>();
        for (Document document : findIterable)
            results.add(document);
        return results;
    }

    /**
     * 通用的mongo计数接口
     *
     * @param query
     * @return
     */
    public Long count(ToMongoQuery query) {
        MongoQuery mongoQuery = null;
        if(query == null || (mongoQuery = query.toMongoQuery()) == null) return 0L;

        MongoCollection<Document> collection = mongoDatabase.getCollection(mongoQuery.getTable());
        Bson filter = mongoQuery.getFilter() != null ? mongoQuery.getFilter() : new BsonDocument();
        CountOptions countOptions = new CountOptions();
        if(mongoQuery.getOffset() != null && mongoQuery.getOffset() >= 0 && mongoQuery.getSize() != null && mongoQuery.getSize() > 0)
            countOptions.skip(mongoQuery.getOffset()).limit(mongoQuery.getSize());
        return collection.count(filter, countOptions);
    }
}
