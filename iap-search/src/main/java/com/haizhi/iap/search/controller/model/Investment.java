package com.haizhi.iap.search.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by chenbo on 17/2/15.
 */
@Data
@NoArgsConstructor
public class Investment {
    @JsonProperty("being_invested")
    DataItem beingInvested;

    DataItem invest;

    @JsonProperty("invest_events")
    DataItem investEvents;

    @JsonProperty("financial_events")
    DataItem financialEvents;

    @JsonProperty("acquirer_events")
    AcquirerEvents acquirerEvents;

    @JsonProperty("exit_events")
    DataItem exitEvents;
}
