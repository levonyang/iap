package com.haizhi.iap.tag.param;

public class QueryESByNameRequest {

    String esType;
    String esIndexName;
    Integer from;
    Integer size;

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

    public Integer getFrom() {
        return from;
    }

    public void setFrom(Integer from) {
        this.from = from;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "QueryESByNameRequest{" +
                "esType='" + esType + '\'' +
                ", esIndexName='" + esIndexName + '\'' +
                ", from=" + from +
                ", size=" + size +
                '}';
    }
}
