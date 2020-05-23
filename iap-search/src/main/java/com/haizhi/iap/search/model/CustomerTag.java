package com.haizhi.iap.search.model;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class CustomerTag {
    Long id;

    @JsonProperty("customer_id")
    String customerId;

    @JsonProperty("name")
    String name;
    
    @JsonProperty("tags")
    String tags;
    
    @JsonProperty("updated_time")
    String updatedTime;

    @JsonProperty("created_time")
    String createdTime;
}
