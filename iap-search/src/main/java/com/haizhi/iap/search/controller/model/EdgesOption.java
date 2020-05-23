package com.haizhi.iap.search.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by chenbo on 17/2/23.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EdgesOption implements Cloneable {
    @JsonProperty("class")
    String category;

    @JsonProperty("trace_depth")
    Integer traceDepth;

    Boolean visible;

    @Override
    protected Object clone(){
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
