package com.haizhi.iap.follow.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by haizhi on 2017/12/21.
 */
public class ChanceSeaInfo {

    private Long id;
    private String title;
    @JsonProperty("rule_name")
    private String ruleName;
    private String company;
    @JsonProperty("master_company")
    private String masterCompany;
    private String level;
    private Integer type;
    private Boolean collected;

    private String desc;
    private String relationship;
    private String detail;
    @JsonProperty("push_time")
    private String pushTime;

    @JsonProperty("type_cn_name")
    private String typeCnName;

    @JsonProperty("type_en_name")
    private String typeEnName;

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Boolean getCollected() {
        return collected;
    }

    public void setCollected(Boolean collected) {
        this.collected = collected;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
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

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getPushTime() {
        return pushTime;
    }

    public void setPushTime(String pushTime) {
        this.pushTime = pushTime;
    }

    public String getTypeCnName() {
        return typeCnName;
    }

    public void setTypeCnName(String typeCnName) {
        this.typeCnName = typeCnName;
    }

    public String getTypeEnName() {
        return typeEnName;
    }

    public void setTypeEnName(String typeEnName) {
        this.typeEnName = typeEnName;
    }
}
