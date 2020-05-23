package com.haizhi.iap.search.controller.model2.tab.second;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.haizhi.iap.search.controller.model.DataItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by chenbo on 2017/11/10.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssociatedInfo {

    DataItem concert;

    //股东对外投资及任职
    @JsonProperty("contributor_invest_office")
    DataItem contributorInvestOffice;

    //高管对外投资及任职
    @JsonProperty("key_person_invest_office")
    DataItem keyPersonInvestOffice;

    public enum AssociatedInfoSubType{

        CONCERT, CONTRIBUTOR_INVEST_OFFICE, KEY_PERSON_INVEST_OFFICE;

        public String getName() {
            return this.name().toLowerCase();
        }

        public static boolean contains(String typeName) {
            for (AssociatedInfoSubType type : AssociatedInfoSubType.values()) {
                if (type.getName().equals(typeName)) {
                    return true;
                }
            }
            return false;
        }

        public static AssociatedInfoSubType get(String typeName) {
            for (AssociatedInfoSubType type : AssociatedInfoSubType.values()) {
                if (type.getName().equalsIgnoreCase(typeName)) {
                    return type;
                }
            }
            return null;
        }
    }
}
