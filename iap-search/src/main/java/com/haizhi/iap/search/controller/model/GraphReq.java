package com.haizhi.iap.search.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by chenbo on 17/2/23.
 */
@Data
@NoArgsConstructor
public class GraphReq implements Cloneable {
    GraphOptions options;

    @JsonProperty("from_list")
    List<String> fromList;

    @JsonProperty("depth_list")
    List<String> depthList;

    String to;

    String company;

    @Override
    public Object clone() {
        GraphReq req = null;
        try {
            req = (GraphReq) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        req.setOptions((GraphOptions) options.clone());
        return req;
    }
}
