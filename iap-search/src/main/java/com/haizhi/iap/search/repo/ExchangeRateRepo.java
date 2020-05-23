package com.haizhi.iap.search.repo;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import lombok.Setter;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * Created by chenbo on 2017/10/20.
 */
@Repository
public class ExchangeRateRepo {

    @Qualifier("appMongoDatabase")
    @Autowired
    MongoDatabase appMongoDatabase;

    @Setter
    @Autowired
    RedisRepo redisRepo;

    @Setter
    @Resource(name = "supportCurrencyList")
    List<String> supportCurrencyList;

    public Map<String, Double> getAllExchangeRateMap() {
        Map<String, Double> allExchangeRateMap = redisRepo.getExchangeRateCache();
        if (allExchangeRateMap == null || allExchangeRateMap.size() < supportCurrencyList.size()) {
            allExchangeRateMap = Maps.newHashMap();
            MongoCursor<Document> cursor = appMongoDatabase.getCollection("exchange_rate").find().iterator();
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                allExchangeRateMap.put(doc.get("_id").toString(), (Double) doc.get("currency_rate"));
            }
            redisRepo.pushExchangeRateCache(allExchangeRateMap);
        }
        return allExchangeRateMap;
    }

    public List<Map> getAllExchangeRateList() {
        List<Map> result = Lists.newArrayList();
        MongoCursor<Document> cursor = appMongoDatabase.getCollection("exchange_rate").find().iterator();
        while (cursor.hasNext()) {
            result.add(cursor.next());
        }
        return result;
    }

}
