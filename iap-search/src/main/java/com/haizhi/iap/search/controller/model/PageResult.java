package com.haizhi.iap.search.controller.model;

import java.util.List;
import java.util.Map;

/**
 * Created by haizhi on 2017/7/10.
 */
public class PageResult {
    private List<Map<String, Object>> list;
    private int total;

    public List<Map<String, Object>> getList() {
        return list;
    }

    public void setList(List<Map<String, Object>> list) {
        this.list = list;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
