package com.haizhi.iap.search.controller.model2.tab.first;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.haizhi.iap.search.controller.model.DataItem;
import com.haizhi.iap.search.controller.model2.Counter;
import com.haizhi.iap.search.controller.model2.tab.second.CompanyInfo;
import com.haizhi.iap.search.controller.model2.tab.second.CustomsInfo;
import com.haizhi.iap.search.controller.model2.tab.second.ListInfo;
import lombok.Data;

/**
 * 一级tab--基本信息
 * Created by chenbo on 2017/11/7.
 */
@Data
public class BasicInfo extends Counter {

    @JsonProperty("company_info")
    CompanyInfo companyInfo;

    @JsonProperty("annual_report")
    DataItem annualReport;

    @JsonProperty("list_info")
    ListInfo listInfo;

    @JsonProperty("customs_info")
    CustomsInfo customsInfo;

    public enum BasicSubType {

        COMPANY_INFO, ANNUAL_REPORT, LIST_INFO, CUSTOMS_INFO;

        public String getName() {
            return this.name().toLowerCase();
        }

        public static boolean contains(String typeName) {
            for (BasicSubType type : BasicSubType.values()) {
                if (type.getName().equals(typeName)) {
                    return true;
                }
            }
            return false;
        }

        public static BasicSubType get(String typeName) {
            for (BasicSubType type : BasicSubType.values()) {
                if (type.getName().equalsIgnoreCase(typeName)) {
                    return type;
                }
            }
            return null;
        }
    }

}
