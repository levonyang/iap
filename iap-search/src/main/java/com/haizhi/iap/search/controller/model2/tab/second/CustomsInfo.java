package com.haizhi.iap.search.controller.model2.tab.second;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.haizhi.iap.search.controller.model.DataItem;
import com.haizhi.iap.search.controller.model2.Counter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by chenbo on 2017/11/20.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomsInfo extends Counter {

    @JsonProperty("register_info")
    DataItem registerInfo;

    @JsonProperty("credit_rank")
    DataItem creditRank;

    public enum CustomsInfoSubType {

        REGISTER_INFO, CREDIT_RANK;

        public String getName() {
            return this.name().toLowerCase();
        }

        public static boolean contains(String typeName) {
            for (CustomsInfoSubType type : CustomsInfoSubType.values()) {
                if (type.getName().equals(typeName)) {
                    return true;
                }
            }
            return false;
        }

        public static CustomsInfoSubType get(String typeName) {
            for (CustomsInfoSubType type : CustomsInfoSubType.values()) {
                if (type.getName().equalsIgnoreCase(typeName)) {
                    return type;
                }
            }
            return null;
        }
    }
}
