package com.haizhi.iap.search.controller.model;

import java.util.Map;

/**
 * Created by haizhi on 2017/11/23.
 */
public class PageArangoParam {

    private String aql;
    private String aqlCount;
    private Map<String, Object> bindVars;

    public PageArangoParam(String aql, String aqlCount, Map<String, Object> bindVars) {
        this.aql = aql;
        this.aqlCount = aqlCount;
        this.bindVars = bindVars;
    }

    public String getAql() {
        return aql;
    }

    public void setAql(String aql) {
        this.aql = aql;
    }

    public String getAqlCount() {
        return aqlCount;
    }

    public void setAqlCount(String aqlCount) {
        this.aqlCount = aqlCount;
    }

    public Map<String, Object> getBindVars() {
        return bindVars;
    }

    public void setBindVars(Map<String, Object> bindVars) {
        this.bindVars = bindVars;
    }
}
