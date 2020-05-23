package com.haizhi.iap.search.enums;

/**
 * Created by chenbo on 17/2/13.
 */
public enum EnterpriseSearchType {
    /**工商信息、企业年报、上市公司、投资关系、知识产权、招标信息、风险信息、舆情信息*/
    BASIC, ANNUAL_REPORT, LIST, INVESTMENT, INTELLECTUAL_PROPERTY, BIDDING, RISK, PUBLIC_SENTIMENT, INVESTMENT_INSTITUTION,

    //用于一次性返回计数专门给出一个type
    ALL;

    public String getName(){
        return this.name().toLowerCase();
    }
}
