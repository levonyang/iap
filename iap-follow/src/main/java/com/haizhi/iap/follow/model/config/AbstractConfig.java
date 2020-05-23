package com.haizhi.iap.follow.model.config;

/**
 * Created by chenbo on 2017/12/8.
 */

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Map;

public abstract class AbstractConfig {

    Long id;

    @JsonIgnore
    Long userId;

    String name;

    Integer type;

    @JsonIgnore
    Map<String, Object> param;

    Integer enable;

    public AbstractConfig() {
        this.enable = 1;
    }

    abstract public Integer getType();

    abstract public String getName();

    abstract public Map<String, Object> getParam();

    abstract public void setParam(Map<String, Object> param);

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getEnable() {
        return enable;
    }

    public void setEnable(Integer enable) {
        this.enable = enable;
    }

}
