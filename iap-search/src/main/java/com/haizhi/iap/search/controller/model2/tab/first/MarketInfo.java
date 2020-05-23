package com.haizhi.iap.search.controller.model2.tab.first;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.haizhi.iap.search.controller.model.DataItem;
import com.haizhi.iap.search.controller.model2.Counter;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 一级tab -- 营销信息
 * Created by chenbo on 2017/11/8.
 */
@Data
@NoArgsConstructor
public class MarketInfo extends Counter {
    //招标信息
    @JsonProperty("bid_info")
    DataItem bidInfo;

    //中标信息
    @JsonProperty("win_info")
    DataItem winInfo;

    //土地招拍挂信息
    @JsonProperty("land_auction")
    DataItem landAuction;

    //专利信息
    DataItem patent;

    //纳税等级为A
    @JsonProperty("tax_payer_level_a")
    DataItem taxPayerLevelA;

    //招聘公告
    @JsonProperty("hiring_info")
    DataItem hiringInfo;

    public enum MarketSubType {

        BID_INFO, WIN_INFO, LAND_AUCTION, PATENT, TAX_PAYER_LEVEL_A, HIRING_INFO;

        public String getName() {
            return this.name().toLowerCase();
        }

        public static boolean contains(String typeName) {
            for (MarketSubType type : MarketSubType.values()) {
                if (type.getName().equals(typeName)) {
                    return true;
                }
            }
            return false;
        }

        public static MarketSubType get(String typeName) {
            for (MarketSubType type : MarketSubType.values()) {
                if (type.getName().equalsIgnoreCase(typeName)) {
                    return type;
                }
            }
            return null;
        }
    }
}
