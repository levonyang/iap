package com.haizhi.iap.follow.model.config.event.market;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import com.haizhi.iap.follow.model.config.AbstractConfig;

import java.util.List;

/**
 * Created by chenbo on 2017/12/8.
 */
public abstract class MarketEventConfig extends AbstractConfig {

    @JsonIgnore
    MarketEventType marketEventType;

    @Override
    public Integer getType() {
        return this.marketEventType.getCode();
    }

    @Override
    public String getName() {
        return this.marketEventType.getName();
    }

    public enum MarketEventType {
        SHAREHOLDER_LISTED(201, "企业股东中新出现上市企业"), TAX_LEVEL_A(202, "企业为A级纳税人"),

        BIDDING(203, "企业中标"), NEW_BRANCH(204, "企业新增分支机构"), NEW_INVEST(205, "企业新增对外投资");

        private Integer code;

        private String name;

        MarketEventType(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        public Integer getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public static boolean contains(Integer code) {
            for (MarketEventType type : MarketEventType.values()) {
                if (type.getCode().equals(code)) {
                    return true;
                }
            }
            return false;
        }

        public static List<Integer> allCode() {
            List<Integer> typeList = Lists.newArrayList();
            for (MarketEventType type : MarketEventType.values()) {
                typeList.add(type.getCode());
            }
            return typeList;
        }
    }
}
