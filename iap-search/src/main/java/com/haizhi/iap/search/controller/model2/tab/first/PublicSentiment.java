package com.haizhi.iap.search.controller.model2.tab.first;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.haizhi.iap.search.controller.model2.Counter;
import com.haizhi.iap.search.controller.model2.tab.second.BaiduNews;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 一级tab -- 舆情信息
 * Created by chenbo on 2017/11/8.
 */
@Data
@NoArgsConstructor
public class PublicSentiment extends Counter {
    //TODO 关联行业新闻
//    @JsonProperty("associate_news")
//    DataItem associateNews;

    //自身新闻
    @JsonProperty("news")
    BaiduNews baiduNews;

    public enum SentimentSubType{

        NEWS;

        public String getName() {
            return this.name().toLowerCase();
        }

        public static boolean contains(String typeName) {
            for (SentimentSubType type : SentimentSubType.values()) {
                if (type.getName().equals(typeName)) {
                    return true;
                }
            }
            return false;
        }

        public static SentimentSubType get(String typeName) {
            for (SentimentSubType type : SentimentSubType.values()) {
                if (type.getName().equalsIgnoreCase(typeName)) {
                    return type;
                }
            }
            return null;
        }
    }
}
