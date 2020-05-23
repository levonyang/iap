package com.haizhi.iap.configure.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Author dmy
 * @Date 2017/4/7 下午4:46.
 */
@Data
@NoArgsConstructor
public class SecondMenu {
    Long id;

    String name;

    Integer order;

    @JsonProperty("first_menu_id")
    Long firstMenuId;

    @JsonProperty("create_time")
    Date createTime;

    @JsonProperty("update_time")
    Date updateTime;

    @JsonProperty("component")
    Component component;

}
