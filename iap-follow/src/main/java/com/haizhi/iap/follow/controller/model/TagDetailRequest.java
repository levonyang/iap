package com.haizhi.iap.follow.controller.model;

import lombok.Data;

import java.util.Map;

@Data
public class TagDetailRequest {
    private String fieldName;
    private Map<String, Object> op;
    private float boost = (float) 1.0;
}
