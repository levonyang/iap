package com.haizhi.iap.search.controller.model2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by chenbo on 2017/11/8.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {
    String name;

    @JsonProperty("stock_code")
    String stockCode;

    String type; //具体值看SearchType

    @JsonProperty("sub_type")
    String subType;

    @JsonProperty("third_type")
    String thirdType;

    @JsonProperty("year_quarter")
    String yearQuarter;

    @JsonProperty("only_count")
    Boolean onlyCount;

    Integer offset;

    Integer count;
}
