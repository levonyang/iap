package com.haizhi.iap.search.controller.model2.tab.second;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.haizhi.iap.search.controller.model.DataItem;
import com.haizhi.iap.search.controller.model2.Counter;
import lombok.Data;

/**
 * Created by chenbo on 2017/11/8.
 */
@Data
public class AllPenalty extends Counter {

    DataItem penalty;

    //海关行政处罚
    @JsonProperty("customs_penalty")
    DataItem customsPenalty;

    //TODO环保行政处罚

}
