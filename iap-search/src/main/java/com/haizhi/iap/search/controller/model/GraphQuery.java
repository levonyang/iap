package com.haizhi.iap.search.controller.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Created by chenbo on 17/2/24.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraphQuery {

    public GraphQuery(String query, Map<String, Object> bindVars) {
        this.query = query;
        this.bindVars = bindVars;
    }

    String query;

    Map<String, Object> bindVars;

    private int batchSize = 1000;
}
