package com.haizhi.iap.search.utils;

import com.haizhi.iap.search.conf.AppDataCollections;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * @author weimin
 * @description 汇率类
 * @date 2018-12-25
 */
@Service
public class ExchangeRateTransformer {

    @Autowired
    @Qualifier("appMongoDatabase")
    private MongoDatabase appMongoDatabase;

    private double getExchangeRateByCurrencyCd(String currencyCd) {

        Document first = appMongoDatabase.getCollection(AppDataCollections.COLL_EXCHANGE_RATE)
                .find(Filters.eq("_id", currencyCd))
                .projection(Projections.fields(
                        Projections.include("currency_rate"),
                        Projections.exclude("_id")))
                .first();

        return first == null ? 0d : Math.max(first.getDouble("currency_rate"), 0d);
    }


    /**
     * * @description 转换成人民币
     *
     * @param currencyCd
     * @param amount
     * @return java.lang.Double
     * @author weimin
     * @date 2018-12-25
     */
    public double fromOtherToRMB(String currencyCd, double amount) {
        double exchangeRate = getExchangeRateByCurrencyCd(currencyCd);
        return exchangeRate * amount;
    }

    /**
     * @param currencyCd
     * @param amount
     * @return java.lang.Double
     * @description 转换成其他币种
     * @author weimin
     * @date 2018-12-25
     */
    public Double fromRMBToOther(String currencyCd, Double amount) {
        double exchangeRate = getExchangeRateByCurrencyCd(currencyCd);
        return exchangeRate == 0d ? 0d : amount / exchangeRate;
    }


}
