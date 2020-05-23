package com.haizhi.iap.search.controller.model2.tab.first;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.haizhi.iap.search.controller.model.AcquirerEvents;
import com.haizhi.iap.search.controller.model.DataItem;
import com.haizhi.iap.search.controller.model2.Counter;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 一级tab--关联关系
 * Created by chenbo on 2017/11/7.
 */
@Data
@NoArgsConstructor
public class AssociatedRelation extends Counter {

    //对外投资
    DataItem invest;

    //投资事件
    @JsonProperty("invest_events")
    DataItem investEvents;

    //融资事件
    @JsonProperty("financial_events")
    DataItem financialEvents;

    //并购事件
    @JsonProperty("acquirer_events")
    AcquirerEvents acquirerEvents;

    //退出事件
    @JsonProperty("exit_events")
    DataItem exitEvents;

    //一致行动关系
    DataItem concert;

    //股东对外投资及任职
    @JsonProperty("contributor_invest_office")
    DataItem contributorInvestOffice;

    //高管对外投资及任职
    @JsonProperty("key_person_invest_office")
    DataItem keyPersonInvestOffice;

    public enum RelationSubType {
        INVEST, INVEST_EVENTS, FINANCIAL_EVENTS, ACQUIRER_EVENTS, EXIT_EVENTS, CONCERT, CONTRIBUTOR_INVEST_OFFICE,

        KEY_PERSON_INVEST_OFFICE;

        public String getName(){
            return this.name().toLowerCase();
        }

        public static boolean contains(String typeName){
            for (RelationSubType type : RelationSubType.values()) {
                if(type.getName().equals(typeName)){
                    return true;
                }
            }
            return false;
        }

        public static RelationSubType get(String typeName) {
            for (RelationSubType type : RelationSubType.values()) {
                if(type.getName().equalsIgnoreCase(typeName)){
                    return type;
                }
            }
            return null;
        }
    }
}
