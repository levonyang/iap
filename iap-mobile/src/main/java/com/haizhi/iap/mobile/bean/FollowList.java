package com.haizhi.iap.mobile.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    /**
     * 默认关注列表名（移动端没有关注分组/关注列表这个设计，所以把关注的所有企业都放到默认列表中）
     */
    @JsonIgnore
    public static final String DEFAULT_NAME = "mobile";

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
