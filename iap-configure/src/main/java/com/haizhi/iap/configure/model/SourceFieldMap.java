package com.haizhi.iap.configure.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Author dmy
 * @Date 2017/4/13 下午4:59.
 */
@Data
@NoArgsConstructor
public class SourceFieldMap {

    Long id;

    @JsonProperty("source_config_id")
    Long sourceConfigId;

    //对应的数据源字段名
    @JsonProperty("source_field")
    String sourceField;

    //是否主键
    @JsonProperty("is_key")
    int isKey;

    //起的中文名
    String name;

    @JsonProperty("create_time")
    Date createTime;

    @JsonProperty("update_time")
    Date updateTime;
}
