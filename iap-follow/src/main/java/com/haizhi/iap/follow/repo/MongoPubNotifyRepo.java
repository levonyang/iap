package com.haizhi.iap.follow.repo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.haizhi.iap.follow.model.NotifyEventInfo;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
* @description 操作monogodb notify_data public_notification表的对象
* @author LewisLouis
* @date 2018/8/31
*/
@Repository
@Slf4j
public class MongoPubNotifyRepo {

    /**
     * 消息存储表
     */
    private static final String PUB_NOTIFY_COLLECTION = "public_notification";


    /**
     * monogodb notify_data数据库的操作类
     */
    @Setter
    @Autowired
    @Qualifier("notifyMongoDatabase")
    MongoDatabase notifyMongoDatabase;


    /**
    * @description 根据条件查询风险和机会和事件信息
    * @param companies 公司名称列表
    * @param ruleType 规则类型 0:风险 1:营销（机会）
    * @param offSet 数据偏移量
    * @param count 分页大小
    * @return java.util.List<com.haizhi.iap.follow.model.NotifyEventInfo>
    * @author LewisLouis
    * @date 2018/9/2
    */
    public List<NotifyEventInfo> findEventInfoByPage(List<String> companies, Integer ruleType,
                                                     Integer offSet, Integer count) {

        MongoCollection<Document> mongoCollection = notifyMongoDatabase.getCollection(PUB_NOTIFY_COLLECTION);

        List<NotifyEventInfo> notifyEventInfos = new ArrayList<>();
        BasicDBList queryList = new BasicDBList();
        for(String value : companies) {
            BasicDBObject filter = new BasicDBObject();
            filter.put("company", value);
            queryList.add(filter);
        }

        BasicDBObject query = new BasicDBObject();
        query.put("ruleType",ruleType);
        query.put("$or", queryList);

        BasicDBObject sort = new BasicDBObject();
        sort.put("pushTime",-1);

        MongoCursor<Document> docs = mongoCollection.find(query).skip(offSet).limit(count).sort(sort).iterator();
        Document doc = null;
        NotifyEventInfo info = null;
        while (docs.hasNext()){
           doc = docs.next();
           info =  JSON.parseObject(JSON.toJSONString(doc),NotifyEventInfo.class);
           info.setDetails(JSONArray.parseObject(info.getDetail(), Map.class));
           notifyEventInfos.add(info);
        }
        return notifyEventInfos;
    }



    /**
    * @description 根据查询条件查询风险和机会事件信息数量
    * @param companys 公司名称列表
    * @param ruleType 规则类型 0:风险 1:营销（机会）
    * @return java.lang.Long 数据数量
    * @author LewisLouis
    * @date 2018/9/2
    */
    public Long findEventInfoCount(List<String> companys, Integer ruleType) {

        MongoCollection<Document> mongoCollection = notifyMongoDatabase.getCollection(PUB_NOTIFY_COLLECTION);

        BasicDBList queryList = new BasicDBList();
        for(String value : companys) {
            BasicDBObject filter = new BasicDBObject();
            filter.put("company", value);
            queryList.add(filter);
        }

        BasicDBObject query = new BasicDBObject();
        query.put("ruleType",ruleType);
        query.put("$or", queryList);

        long  dataCount = mongoCollection.count(query);

        return dataCount;
    }

}
