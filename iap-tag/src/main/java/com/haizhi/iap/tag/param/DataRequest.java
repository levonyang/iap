package com.haizhi.iap.tag.param;

import java.util.List;
import java.util.Map;

public class DataRequest {

    public List<Map> datalist;
    String esType;
    String esIndexName;

    public String getEsType() {
        return esType;
    }

    public void setEsType(String esType) {
        this.esType = esType;
    }

    public String getEsIndexName() {
        return esIndexName;
    }

    public void setEsIndexName(String esIndexName) {
        this.esIndexName = esIndexName;
    }

    public List<Map> getDatalist() {
        return datalist;
    }

    public void setDatalist(List<Map> datalist) {
        this.datalist = datalist;
    }
}
