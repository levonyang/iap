package com.haizhi.iap.follow.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by haizhi on 2017/9/27.
 */
public class ReqDelMsg {

    private Long id;

    @JsonProperty("sub_type")
    private Integer subType;

    private Boolean isClosely;

    @JsonProperty("push_time")
    private String pushTime;

    private String company;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getSubType() {
        return subType;
    }

    public void setSubType(Integer subType) {
        this.subType = subType;
    }

    public String getPushTime() {
        return pushTime;
    }

    public void setPushTime(String pushTime) {
        this.pushTime = pushTime;
    }

    public Boolean isClosely() {
        return isClosely;
    }

    public void setIsClosely(Boolean closely) {
        isClosely = closely;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }
}
