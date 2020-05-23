package com.haizhi.iap.search.controller.model;

import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by chenbo on 17/4/25.
 */
@Data
@NoArgsConstructor
public class AdvancedSearchReq {
    @JsonProperty("key_word")
    private String keyWord = "";

    @JsonProperty("location_map")
    Map<String, List<Map<String, List<String>>>> locationMap;

    @JsonProperty("registered_capital")
    Range<String> registeredCapital;

    List<String> industries;

    @JsonProperty("registered_time")
    Range<String> registeredTime;

    @JsonProperty("business_status")
    List<String> businessStatus;

    @JsonProperty("investment_num")
    Range<String> investmentNum;

    @JsonProperty("shareholders_num")
    Range<String> shareholdersNum;

    @JsonProperty("shareholders_change")
    Range<String> shareholdersChange;

    @JsonProperty("company_name_change")
    Range<String> companyNameChange;

    @JsonProperty("legal_man_change")
    Range<String> legalManChange;

    @JsonProperty("address_change")
    Range<String> addressChange;

    @JsonProperty("member_change")
    Range<String> memberChange;

    @JsonProperty("rule_change")
    Range<String> ruleChange;

    @JsonProperty("scope_change")
    Range<String> scopeChange;

    @JsonProperty("pattern_num")
    Range<String> patternNum;

    @JsonProperty("win_bid_num")
    Range<String> winBidNum;

    @JsonProperty("acting_bid_num")
    Range<String> actingBidNum;

    @JsonProperty("bid_num")
    Range<String> bidNum;

    @JsonProperty("plaintiff_num")
    Range<String> plaintiffNum;

    @JsonProperty("defendant_num")
    Range<String> defendantNum;

    @JsonProperty("executed_money")
    Range<String> executedMoney;

    @JsonProperty("executed_count")
    Range<String> executedCount;

    @JsonProperty("tax_arrears")
    Range<String> taxArrears;

    @JsonProperty("tax_arrears_num")
    Range<String> taxArrearsNum;

    @JsonProperty("a_tax_num")
    Range<String> a_taxNum;

    @JsonProperty("punish_num")
    Range<String> punishNum;

    @JsonProperty("is_listed")
    Boolean isListed;

    @JsonProperty("list_status")
    List<String> listStatus;

    @JsonProperty("list_time")
    Range<String> listTime;

    @JsonProperty("list_sector")
    List<String> listSector;

    @JsonProperty("branch_num")
    Range<String> branchNum;

    @JsonProperty("shareholder_listed")
    Boolean shareholderListed;

    @JsonProperty("offset")
    Integer offset;

    @JsonProperty("count")
    Integer count;

    @JsonProperty("market_coefficient")
    List<String> marketCoefficient;

    @JsonProperty("risk_coefficient")
    List<String> riskCoefficient;

    @JsonProperty("risk_sork")
    Integer riskSort;

    @JsonProperty("market_sort")
    Integer marketSort;
}
