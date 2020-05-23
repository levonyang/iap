package com.haizhi.iap.search.controller.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.List;

/**
 * Created by haizhi on 2017/7/7.
 */
public class ReqGuaranteeOrTransfer {

    private String from;
    private String to;
    private String type;
    private int offset;
    @JsonProperty("isTwoWay")
    private boolean isTwoWay;

    //limit
    private int count;
    private List<Map<String,Object>> conditionList;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<Map<String, Object>> getConditionList() {
        return conditionList;
    }

    public void setConditionList(List<Map<String, Object>> conditionList) {
        this.conditionList = conditionList;
    }

    public boolean isTwoWay() {
        return isTwoWay;
    }

    @JsonIgnore
    public void setTwoWay(boolean twoWay) {
        isTwoWay = twoWay;
    }
/*
    public void setIsTwoWay(boolean isTwoWay) {
        isTwoWay = isTwoWay;
    }
    */
}
