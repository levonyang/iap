package com.haizhi.iap.search.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Created by chenbo on 17/2/22.
 */
@Data
@NoArgsConstructor
public class GraphCompany {

    String id;

    String key;

    @JsonProperty("business_scope")
    String businessScope;

    Double capital;

    @JsonProperty("capital_formatted")
    Map<String, Object> capitalFormatted;

    @JsonProperty("card_num")
    String cardNum;

    @JsonProperty("enterprise_type")
    String enterpriseType;

    @JsonProperty("group_id")
    String groupId;

    String industry;

    @JsonProperty("legal_man")
    String legalMan;

    String name;

    @JsonProperty("operation_startdate")
    String operationStartDate;

    String highlight;

    @JsonProperty("business_status")
    String businessStatus;

}
