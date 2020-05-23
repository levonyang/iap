package com.haizhi.iap.search.controller.model2.tab.second;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.haizhi.iap.search.controller.model.DataItem;
import com.haizhi.iap.search.controller.model2.Counter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 二级tab--工商信息
 * Created by chenbo on 2017/11/7.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyInfo extends Counter {
    //工商基本信息
    @JsonProperty("gongshang_basic")
    DataItem gongshangBasic;

    //股东信息
    @JsonProperty("shareholder_information")
    DataItem shareholderInfo;

    //高管信息
    @JsonProperty("key_person")
    DataItem keyPerson;

    //分支机构
    @JsonProperty("branch")
    DataItem branches;

    //工商变更
    @JsonProperty("change_records")
    DataItem changeRecords;

    //股权变更
    DataItem changeShareholdingInfo;

    public enum CompanyInfoSubType {

        GONGSHANG_BASIC("gongshang_basic"),
        SHAREHOLDER_INFORMATION("shareholder_information"),
        KEY_PERSON("key_person"),
        BRANCH("branch"),
        CHANGE_RECORDS("change_records"),
        CHANGE_SHAREHOLDING_INFO("changeShareholdingInfo"),
        ;

        private String type;

        CompanyInfoSubType(String type) {
            this.type = type;
        }

        public static boolean contains(String typeName) {
            for (CompanyInfoSubType type : CompanyInfoSubType.values()) {
                if (type.type.equals(typeName)) {
                    return true;
                }
            }
            return false;
        }

        public static CompanyInfoSubType get(String typeName) {
            for (CompanyInfoSubType type : CompanyInfoSubType.values()) {
                if (type.type.equalsIgnoreCase(typeName)) {
                    return type;
                }
            }
            return null;
        }
    }

}
