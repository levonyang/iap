package com.haizhi.iap.configure.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Author dmy
 * @Date 2017/5/8 下午5:22.
 */
@Data
@NoArgsConstructor
public class Param {
    Long id;

    @JsonProperty("datasource_id")
    Long datasourceId;

    @JsonIgnore
    SourceFieldMap sourceFieldMap;

    @JsonProperty("order_key")
    Long orderKey;

    @JsonProperty("order_field_name")
    String orderFieldName;

    @JsonProperty("is_order")
    Integer isOrder;

    @JsonProperty("is_desc")
    Integer isDesc;

    @JsonProperty("component_id")
    Long componentId;

    @JsonProperty("create_time")
    Date createTime;

    @JsonProperty("update_time")
    Date updateTime;

    Long firstMenuId;
}
