package com.haizhi.iap.follow.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by haizhi on 2017/9/8.
 */
public class ReqEditMsgs {

    private Boolean isClosely;
    private Boolean isAllEdit;
    private Boolean collected;
    private Boolean read;

    private String type;
    private List<Integer> subType = new ArrayList<>();

    private List<Long> idList = new ArrayList<>();

    public Boolean isClosely() {
        return isClosely;
    }

    public void setIsClosely(Boolean closely) {
        isClosely = closely;
    }

    public Boolean isAllEdit() {
        return isAllEdit;
    }

    public void setIsAllEdit(Boolean allEdit) {
        isAllEdit = allEdit;
    }

    public Boolean getCollected() {
        return collected;
    }

    public void setCollected(Boolean collected) {
        this.collected = collected;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
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

    public List<Long> getIdList() {
        return idList;
    }

    public void setIdList(List<Long> idList) {
        this.idList = idList;
    }
}
