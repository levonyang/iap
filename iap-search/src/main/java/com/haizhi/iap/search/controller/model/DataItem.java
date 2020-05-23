package com.haizhi.iap.search.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.haizhi.iap.search.controller.model2.Counter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by chenbo on 17/2/13.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataItem extends Counter {
    Object data;

    List<String> tab;

    @JsonProperty("tab_count")
    Long tabCount;

    @JsonProperty("total_count")
    Long totalCount;

    public DataItem(Object data, Long totalCount) {
        this(data, null, null, totalCount);
    }

}
