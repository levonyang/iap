package com.haizhi.iap.follow.model;

import com.haizhi.iap.follow.enums.DataType;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by haizhi on 2017/8/31.
 */
public class ReqGetMsgs {

    private Boolean read;
    private Boolean collected;
    private String type;
    private List<Integer> subType = new ArrayList<>();

    private Integer offset = 0;
    private Integer count = 10;

    private String company;
    private String masterCompany;

    private Boolean isClosely;

    private String pushTime;
    private String dataType = DataType.MONTH;

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getPushTime() {
        return pushTime;
    }

    public void setPushTime(String pushTime) {
        this.pushTime = pushTime;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public Boolean getCollected() {
        return collected;
    }

    public void setCollected(Boolean collected) {
        this.collected = collected;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Integer> getSubType() {
        return subType;
    }

    public void setSubType(List<Integer> subType) {
        this.subType = subType;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getMasterCompany() {
        return masterCompany;
    }

    public void setMasterCompany(String masterCompany) {
        this.masterCompany = masterCompany;
    }

    public Boolean isClosely() {
        return isClosely;
    }

    public void setIsClosely(Boolean closely) {
        isClosely = closely;
    }
}
