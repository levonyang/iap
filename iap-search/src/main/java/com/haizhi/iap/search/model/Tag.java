package com.haizhi.iap.search.model;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class Tag {
    Long id;

    String category;
    
    @JsonProperty("en_name")
    String enName;

    @JsonProperty("cn_name")
    String cnName;
    
    @JsonProperty("category_id")
    Long categoryId;

    @JsonProperty("desc")
    String desc;
    
    @JsonProperty("value_type")
    Integer valueType;
    
    @JsonProperty("value_sets")
    String valueSets;
    
    Integer enabled;
    
    @JsonProperty("updated_time")
    String updatedTime;

    @JsonProperty("created_time")
    String createdTime;
}
