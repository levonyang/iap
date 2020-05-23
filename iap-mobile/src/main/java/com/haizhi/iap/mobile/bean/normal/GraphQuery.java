package com.haizhi.iap.mobile.bean.normal;

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
public class GraphQuery
{

    public GraphQuery(String query, Map<String, Object> bindVars) {
        this.query = query;
        this.bindVars = bindVars;
    }

    public GraphQuery(String query, Map<String, Object> bindVars, int batchSize) {
        this.query = query;
        this.bindVars = bindVars;
        this.batchSize = batchSize;
    }

    String query;

    Map<String, Object> bindVars;

    private int batchSize = 1000;
    private Boolean count = false;
}
