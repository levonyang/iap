package com.haizhi.iap.search.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.haizhi.iap.search.model.Tab;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by chenbo on 17/2/14.
 */
@Data
@NoArgsConstructor
public class Listing {
    @JsonProperty("stock_code")
    List<String> stockCodeList;

    @JsonProperty("financial_report")
    DataItem financialReport;

    @Tab
    @JsonProperty("fund_table")
    DataItem fundTable;

    DataItem listing;

    DataItem managers;

    DataItem notice;

    DataItem rules;

    @JsonProperty("ssgs_regular_report")
    DataItem ssgsRegularReport;

    @Tab
    @JsonProperty("top_ten_shareholders")
    DataItem topTenShareholders;

    @Tab
    @JsonProperty("top_ten_tradable_shareholders")
    DataItem topTenTradableShareholders;
}
