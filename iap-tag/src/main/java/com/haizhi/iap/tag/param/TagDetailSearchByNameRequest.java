package com.haizhi.iap.tag.param;

import java.util.List;

public class TagDetailSearchByNameRequest {

    List<TagDetailRequest> tagDetailRequestsList;
    Integer from;
    Integer size;
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

    public List<TagDetailRequest> getTagDetailRequestsList() {
        return tagDetailRequestsList;
    }

    public void setTagDetailRequestsList(List<TagDetailRequest> tagDetailRequestsList) {
        this.tagDetailRequestsList = tagDetailRequestsList;
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
}
