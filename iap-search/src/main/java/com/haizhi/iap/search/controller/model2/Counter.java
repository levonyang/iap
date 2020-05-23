package com.haizhi.iap.search.controller.model2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by chenbo on 2017/11/8.
 */
@Data
public class Counter implements Serializable {

    @JsonProperty("total_count")
    Long totalCount;

}
