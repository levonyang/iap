package com.haizhi.iap.follow.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Created by chenbo on 17/1/16.
 */
@Data
@NoArgsConstructor
public class TypeQuery {
    Long id;

    @JsonProperty("user_id")
    Long userId;

    @JsonProperty("type_name")
    String typeName;

    String query;

    String collection;

    @JsonProperty("company_param")
    String companyParam;

    @JsonProperty("time_param")
    String timeParam;

    @JsonProperty("create_time")
    Date createTime;

    @JsonProperty("update_time")
    Date updateTime;


    String extraFilter;  //附加过滤条件

}
