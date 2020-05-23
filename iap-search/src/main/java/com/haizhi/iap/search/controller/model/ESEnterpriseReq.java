package com.haizhi.iap.search.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by chenbo on 16/12/26.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ESEnterpriseReq {
    @JsonProperty("key_word")
    private String keyWord = "";

    @JsonProperty("type")
    private String searchType;

    private String province;

    private String city;

    private String industry;

    @JsonProperty("offset")
    private Integer from;

    @JsonProperty("count")
    private Integer size;

    @JsonProperty("registered_capital_min")
    private Double registerFoundMin;

    @JsonProperty("registered_capital_max")
    private Double registerFoundMax;

    @JsonProperty("registered_date_min")
    private Long registerDateStart;

    @JsonProperty("registered_date_max")
    private Long registerDateEnd;
}
