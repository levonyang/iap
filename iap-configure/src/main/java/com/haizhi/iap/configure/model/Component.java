package com.haizhi.iap.configure.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * @Author dmy
 * @Date 2017/4/5 上午11:26.
 */
@Data
@NoArgsConstructor
public class Component {

    Long id;

    String name;

    //组件类型
    Integer type;

    @JsonProperty("first_menu_id")
    Long firstMenuId;

    @JsonProperty("second_menu_id")
    Long secondMenuId;

    @JsonProperty("create_time")
    Date createTime;

    @JsonProperty("update_time")
    Date updateTime;

    @JsonProperty("datasource_ids")
    String datasourceIds;

    @JsonProperty("datasource_list")
    List<DataSourceConfig> dataSourceConfigList;

    List<Item> items;

    //卡片类型组件需要，可为空
    List<Item> topItems;

    //计算器
    List<Item> countItems;

    //每页显示的数量--分页
    @JsonProperty("page_count")
    Integer pageCount;

    //是否开启分页
    @JsonProperty("is_page")
    Integer isPage;

    Param param;
}
