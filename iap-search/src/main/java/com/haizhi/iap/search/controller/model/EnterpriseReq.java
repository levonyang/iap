package com.haizhi.iap.search.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by chenbo on 17/2/13.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnterpriseReq {

    String name;

    @JsonProperty("stock_code")
    String stockCode;

//    @JsonProperty("stock_type")
//    String stockType;

    String type;

    @JsonProperty("sub_type")
    String subType;

    @JsonProperty("third_type")
    String thirdType;

    @JsonProperty("year_quarter")
    String yearQuarter;

    @JsonProperty("only_counting")
    Boolean onlyCounting;

    Integer offset;

    Integer count;
}
