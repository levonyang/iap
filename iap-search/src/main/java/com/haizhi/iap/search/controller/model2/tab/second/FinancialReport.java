package com.haizhi.iap.search.controller.model2.tab.second;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Created by chenbo on 17/2/14.
 */
@Data
@NoArgsConstructor
public class FinancialReport {
    /**
     * 综合能力指标
     */
    @JsonProperty("company_ability")
    Map<Integer, Map<String, Object>> company_ability;

    /**
     * 现金流量表
     */
    @JsonProperty("cash_flow")
    Map<Integer, Map<String, Object>> cashFlow;

    /**
     * 资产负债表
     */
    @JsonProperty("assets_liability")
    Map<Integer, Map<String, Object>> assetsLiability;

    /**
     * 利润表
     */
    Map<Integer, Map<String, Object>> profit;
}
