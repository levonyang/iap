package com.haizhi.iap.configure.model;

import lombok.Data;

import java.util.Map;

@Data
public class SearchGraphParam {

    private String table;

    private String from;

    private String to;

    private Map<String, Object> filters;

    private int offset;

    private int limit;

    private Boolean needCount;
}
