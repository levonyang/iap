package com.haizhi.iap.search.enums;

/**
 * Created by chenbo on 17/2/23.
 */
public enum EdgeOptionCategory {
    INVEST, SHAREHOLDER, OFFICER, FAMILY, CONCERT, ACTUAL_CONTROL, PLAINTIFF, DEFENDANT, DISHONEST_EXECUTED,

    PUBLISH, SUBMIT, ACCUSED, WIN_BID, PUBLISH_BID, AGENT_BID, PERSON_MERGE_SUGGEST, SUE, TRADABLE_SHARE,

    //担保, 上下游 实际控制人 控股股东 企业派系
    GUARANTEE, MONEY_FLOW, UPSTREAM, ACTUAL_CONTROLLER, CONTROL_SHAREHOLDER, COMPANY_GROUP, INDIRECT_INVEST;

    public String getName(){
        return this.name().toLowerCase();
    }
}
