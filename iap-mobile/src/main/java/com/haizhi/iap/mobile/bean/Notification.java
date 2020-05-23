package com.haizhi.iap.mobile.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Notification
{
    Long id;

    String title;
    
    @JsonProperty("user_id")
    String userId;

    @JsonProperty("rule_name")
    String ruleName;
    
    @JsonProperty("level")
    String level;

    @JsonProperty("desc")
    String desc;
    
    @JsonProperty("type")
    Integer type;
    
    @JsonProperty("company")
    String company;
    
    String detail;
    
    @JsonProperty("push_time")
    String pushTime;

}
