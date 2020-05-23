package com.haizhi.iap.configure.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Author dmy
 * @Date 2017/4/10 下午3:04.
 */
@Data
@NoArgsConstructor
public class Item {
    Long id;

    String name;

    @JsonProperty("compnent_id")
    Long componentId;

    Long firstMenuId;

    @JsonProperty("source_field_id")
    Long sourceFieldId;

    @JsonProperty("source_field_name")
    String sourceFiledName;

    @JsonProperty("source_field_key")
    String sourceFileKey;

    @JsonProperty("datasource_id")
    Long datasourceId;

    @JsonProperty("datasource_name")
    String datasourceName;

    @JsonProperty("datasource_key")
    String datasourceKey;

    /**
     * 坐标
     */
    Integer x;

    Integer y;

    /**
     * 长宽
     */
    @JsonProperty("col_space")
    Integer colSpace;

    @JsonProperty("row_space")
    Integer rowSpace;

    @JsonProperty("create_time")
    Date createTime;

    @JsonProperty("update_time")
    Date updateTime;

    //1-topItem 0-item 2-countItem
    Integer type;

    //元素类型 0-文本 1-计算器
    @JsonProperty("ele_type")
    Integer eleType;

    //计算方式
    @JsonProperty("count_form")
    String countForm;
}
