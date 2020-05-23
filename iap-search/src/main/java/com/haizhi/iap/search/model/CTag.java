package com.haizhi.iap.search.model;

import lombok.Data;

@Data
public class CTag {

    String tagName;
    String tagValue;
    String tagCategory;
    Integer valueType;
    Boolean hasValue;
}
