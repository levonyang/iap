package com.haizhi.iap.search.controller.model2.tab.second;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.haizhi.iap.search.controller.model.DataItem;
import com.haizhi.iap.search.controller.model2.Counter;
import com.haizhi.iap.search.controller.model2.Sector;
import com.haizhi.iap.search.model.Tab;
import lombok.Data;

import java.util.List;

/**
 * 二级tab--上市信息
 * Created by chenbo on 2017/11/7.
 */
@Data
public class ListInfo extends Counter {
    @JsonProperty("sector_list")
    List<Sector> sectorList;

    //公司概况 listing->basic
    @JsonProperty("listing")
    DataItem listBasic;

    //财务指标
    @JsonProperty("financial_report")
    DataItem financialReport;

    //基金持股
    @Tab
    @JsonProperty("fund_table")
    DataItem fundTable;

    //高管人员
    DataItem managers;

    //公司公告
    DataItem notice;

    //上市章程
    DataItem rules;

    //定期报告
    @JsonProperty("ssgs_regular_report")
    DataItem ssgsRegularReport;

    //十大股东
    @Tab
    @JsonProperty("top_ten_shareholders")
    DataItem topTenShareholders;

    //十大流通股东
    @Tab
    @JsonProperty("top_ten_tradable_shareholders")
    DataItem topTenTradableShareholders;

    public enum ListInfoSubType{

        LISTING, FINANCIAL_REPORT,FUND_TABLE,MANAGERS,NOTICE,RULES,SSGS_REGULAR_REPORT,TOP_TEN_SHAREHOLDERS,

        TOP_TEN_TRADABLE_SHAREHOLDERS;

        public String getName() {
            return this.name().toLowerCase();
        }

        public static boolean contains(String typeName) {
            for (ListInfoSubType type : ListInfoSubType.values()) {
                if (type.getName().equals(typeName)) {
                    return true;
                }
            }
            return false;
        }

        public static ListInfoSubType get(String typeName) {
            for (ListInfoSubType type : ListInfoSubType.values()) {
                if (type.getName().equalsIgnoreCase(typeName)) {
                    return type;
                }
            }
            return null;
        }
    }
}
