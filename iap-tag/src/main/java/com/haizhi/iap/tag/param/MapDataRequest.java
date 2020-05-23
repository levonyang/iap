package com.haizhi.iap.tag.param;

import java.util.List;
import java.util.Map;

public class MapDataRequest {

    public List<Map<String, Object>> datalist;
    String esIndexName;

    public String getEsIndexName() {
        return esIndexName;
    }

    public void setEsIndexName(String esIndexName) {
        this.esIndexName = esIndexName;
    }

    public List<Map<String, Object>> getDatalist() {
        return datalist;
    }

    public void setDatalist(List<Map<String, Object>> datalist) {
        this.datalist = datalist;
    }
}
