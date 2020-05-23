package com.haizhi.iap.search.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Author Lewis
 * @Date 2018/8/2
 */
@Data
@NoArgsConstructor
public class GroupDetail {

    String entityName;  //实体名称，可能是个人或企业公司名称

    String type;  //族谱信息类型,eg:risk_guarantee_info、risk_black_info、market_updown_info、risk_propagation

    String entityId; //图谱关系中实体的唯一ID

    @JsonProperty("group_name")
    String groupName;   //族谱分组名称

    Boolean belongInner;

    Date createTime; //数据插入时间

    Date updateTime; //数据更新时间
}

