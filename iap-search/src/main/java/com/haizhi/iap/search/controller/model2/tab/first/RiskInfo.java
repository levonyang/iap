package com.haizhi.iap.search.controller.model2.tab.first;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.haizhi.iap.search.controller.model.DataItem;
import com.haizhi.iap.search.controller.model2.Counter;
import com.haizhi.iap.search.controller.model2.tab.second.AllPenalty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 一级tab -- 风险信息
 * Created by chenbo on 2017/11/7.
 */
@Data
@NoArgsConstructor
public class RiskInfo extends Counter {
    //开庭公告
    @JsonProperty("court_ktgg")
    DataItem courtKtgg;

    //法院公告
    @JsonProperty("court_fygg")
    DataItem courtFygg;

    //审判流程
    @JsonProperty("judge_process")
    DataItem judgeProcess;

    //法院判决/裁判文书
    @JsonProperty("judgement_wenshu")
    DataItem judgement;

    //欠税公告
    @JsonProperty("owing_tax")
    DataItem owingTax;

    //行政处罚
    @JsonProperty("all_penalty")
    AllPenalty allPenalty;

    //失信人信息
    @JsonProperty("shixin_info")
    DataItem shixinInfo;

    //被执行人信息
    @JsonProperty("zhixing_info")
    DataItem zhixingInfo;

    //排污费征收公告
    @JsonProperty("environment_protection")
    DataItem envInfo;

    //动产抵押
    @JsonProperty("chattel_mortgage_info")
    DataItem chattelMortgageInfo;

    //股权出质
    @JsonProperty("equity_pledged_info")
    DataItem equityPledgedInfo;

    //工商列入经营异常名录信息
    @JsonProperty("abnormal_operation_info")
    DataItem abnormalOperationInfo;

    public enum RiskSubType {
        COURT_KTGG, COURT_FYGG, JUDGE_PROCESS, JUDGEMENT_WENSHU, OWING_TAX, ALL_PENALTY, SHIXIN_INFO, ZHIXING_INFO,

        ENVIRONMENT_PROTECTION, CHATTEL_MORTGAGE_INFO, EQUITY_PLEDGED_INFO, ABNORMAL_OPERATION_INFO;

        public String getName() {
            return this.name().toLowerCase();
        }

        public static boolean contains(String typeName) {
            for (RiskSubType type : RiskSubType.values()) {
                if (type.getName().equals(typeName)) {
                    return true;
                }
            }
            return false;
        }

        public static RiskSubType get(String typeName) {
            for (RiskSubType type : RiskSubType.values()) {
                if (type.getName().equalsIgnoreCase(typeName)) {
                    return type;
                }
            }
            return null;
        }
    }

    public enum RiskThirdType {
        PENALTY, CUSTOMS_PENALTY;

        public String getName() {
            return this.name().toLowerCase();
        }

        public static boolean contains(String typeName) {
            for (RiskThirdType type : RiskThirdType.values()) {
                if (type.getName().equals(typeName)) {
                    return true;
                }
            }
            return false;
        }

        public static RiskThirdType get(String typeName) {
            for (RiskThirdType type : RiskThirdType.values()) {
                if (type.getName().equalsIgnoreCase(typeName)) {
                    return type;
                }
            }
            return null;
        }
    }
}
