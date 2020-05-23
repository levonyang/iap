package com.haizhi.iap.follow.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by haizhi on 2017/12/21.
 */
public class PageInfo {

    private Object data;
    @JsonProperty("total_count")
    private Long count;

    public PageInfo(Object data, Long count) {
        this.data = data;
        this.count = count;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
