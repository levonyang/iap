package com.haizhi.iap.configure.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
public class GraphRelation {

    Integer id;

    String name;

    @JsonProperty("source_config_id")
    Integer sourceConfigId;

    String comment;

    @JsonProperty("comment_name")
    String commentName;

    @JsonProperty("is_selected")
    short isSelected;

    @JsonProperty("is_show")
    short isShow;

    @JsonProperty("is_multi")
    short isMulti;

    String attrs;

    @JsonProperty("target_table")
    String targetTable;

    @JsonProperty("create_time")
    Date createTime;

    @JsonProperty("update_time")
    Date updateTime;
}
