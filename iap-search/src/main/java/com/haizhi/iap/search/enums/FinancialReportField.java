package com.haizhi.iap.search.enums;

/**
 * Created by chenbo on 17/2/14.
 */
public enum FinancialReportField {
    COMPANY_ABILITY, CASH_FLOW, PROFIT, ASSETS_LIABILITY;

    public String getName(){
        return this.name().toLowerCase();
    }
}
