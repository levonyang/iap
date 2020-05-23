package com.haizhi.iap.search.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by chenbo on 17/2/27.
 */
@Data
@NoArgsConstructor
public class InvestInstitution {
    DataItem basic;

    DataItem contact;

    @JsonProperty("manage_funds")
    DataItem manageFunds;

    @JsonProperty("invest_events")
    DataItem investEvents;

    @JsonProperty("exit_events")
    DataItem exitEvents;

}
