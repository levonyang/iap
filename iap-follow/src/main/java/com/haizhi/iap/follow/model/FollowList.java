package com.haizhi.iap.follow.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Created by chenbo on 17/1/11.
 */
@Data
@NoArgsConstructor
public class FollowList{
    Long id;

    String name;

    @JsonProperty("user_id")
    Long userId;

    @JsonProperty("create_time")
    Date createTime;

    @JsonProperty("update_time")
    Date updateTime;

    @JsonProperty("company_in")
    Boolean companyIn;

    @JsonProperty("count")
    Integer listCount;

}
