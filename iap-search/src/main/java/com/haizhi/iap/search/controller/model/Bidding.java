package com.haizhi.iap.search.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by chenbo on 17/2/15.
 */
@Data
@NoArgsConstructor
public class Bidding {

    @JsonProperty("bid_info")
    DataItem bidInfo;

    @JsonProperty("win_info")
    DataItem winInfo;

    @JsonProperty("land_auction")
    DataItem landAuction;
}
