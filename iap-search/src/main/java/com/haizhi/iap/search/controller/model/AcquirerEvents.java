package com.haizhi.iap.search.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by chenbo on 17/2/27.
 */
@Data
@NoArgsConstructor
public class AcquirerEvents {
    DataItem acquirer;

    DataItem acquirered;

    @JsonProperty("total_count")
    Long totalCount;
}
