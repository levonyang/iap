package com.haizhi.iap.search.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by chenbo on 17/2/13.
 */
@Data
@NoArgsConstructor
public class Basic {
    DataItem branch;

    @JsonProperty("change_records")
    DataItem changeRecords;

    @JsonProperty("gongshang_basic")
    DataItem gongshangBasic;

    @JsonProperty("key_person")
    DataItem keyPerson;

    @JsonProperty("shareholder_information")
    DataItem shareholderInformation;

    @JsonProperty("contributor_information")
    DataItem contributorInformation;

    @JsonProperty("abnormal_operation_info")
    DataItem abnormalOperationInfo;

    @JsonProperty("chattel_mortgage_info")
    DataItem chattelMortgageInfo;

    @JsonProperty("equity_pledged_info")
    DataItem equityPledgedInfo;

    @JsonProperty("customs_information")
    DataItem customsInformation;

}
