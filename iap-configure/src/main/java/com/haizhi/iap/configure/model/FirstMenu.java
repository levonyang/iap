package com.haizhi.iap.configure.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * @Author dmy
 * @Date 2017/4/6 下午4:24.
 */
@Data
@NoArgsConstructor
public class FirstMenu {

    Long id;

    String name;

    Integer order;

    @JsonProperty("create_time")
    Date createTime;

    @JsonProperty("update_time")
    Date updateTime;

    @JsonProperty("second_menu_list")
    List<SecondMenu> secondMenus;

    //是否是固定导航
    @JsonProperty("is_fix")
    Integer isFix;
}
