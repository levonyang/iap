package com.haizhi.iap.follow.model.config.event.risk;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.haizhi.iap.follow.model.config.AbstractConfig;

import java.util.List;

/**
 * Created by chenbo on 2017/12/8.
 */
public abstract class RiskEventConfig extends AbstractConfig {

    @JsonProperty("date_range")
    String dateRange;

    @JsonIgnore
    RiskEventType riskEventType;

    public RiskEventConfig() {
        super();
    }

    @Override
    public Integer getType() {
        return this.riskEventType.getCode();
    }

    @Override
    public String getName() {
        return this.riskEventType.getName();
    }

    public String getDateRange() {
        return dateRange;
    }

    public void setDateRange(String dateRange) {
        this.dateRange = dateRange;
    }

    public enum RiskEventType {
        COURT_KTGG(101, "企业关联开庭公告"), COURT_FYGG(102, "企业关联法院公告"), JUDGE_PROCESS(103, "企业关联案件审判流程"),

        JUDGEMENT(104, "企业关联判决结果"), SHIXIN_INFO(105, "企业失信被执行"),

//        OWING_TAX(106, "企业欠税被披露"),

        PENALTY(107, "企业被行政处罚"), BUSINESS_STATUS_UNUSUAL(108, "企业的经营状态变为异常状态"),

        LEGAL_MAN_CHANGE(109, "企业法定代表人频繁变更"), MANAGER_CHANGE(110, "企业高管频繁变更"),

//        REGISTER_CAPITAL_CHANGE(111, "企业注册资本频繁变更"),

        SHAREHOLDER_CHANGE(112, "企业股东频繁变更"),

//        NAME_CHANGE(113, "企业名字频繁变更"),

        ADDRESS_CHANGE(114, "企业经营地址频繁变更");

//        BLIND_EXPAND(115, "盲目扩张");

        private Integer code;

        private String name;

        RiskEventType(int code, String name) {
            this.code = code;
            this.name = name;
        }

        public Integer getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public static boolean contains(Integer code){
            for (RiskEventType type : RiskEventType.values()){
                if(type.getCode().equals(code)){
                    return true;
                }
            }
            return false;
        }

        public static List<Integer> allCode() {
            List<Integer> typeList = Lists.newArrayList();
            for(RiskEventType type : RiskEventType.values()){
                typeList.add(type.getCode());
            }
            return typeList;
        }
    }
}
