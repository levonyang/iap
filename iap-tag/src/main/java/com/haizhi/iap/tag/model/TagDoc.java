package com.haizhi.iap.tag.model;

import lombok.Data;

import java.util.Map;

@Data
public class TagDoc {
    private String id;
    private Map<String, Object> fields;
}
