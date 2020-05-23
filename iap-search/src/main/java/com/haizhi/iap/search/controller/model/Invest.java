package com.haizhi.iap.search.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class Invest{

    @JsonProperty("invest_amount")
    NumberRange investAmount;

    // 投资占比
    @JsonProperty("invest_ratio")
    NumberRange investRatio;

    // 股东占比
    @JsonProperty("shareholder_ratio")
    NumberRange shareholderRatio;

    @JsonProperty("shareholder_type")
    List<String> shareholderType;

}