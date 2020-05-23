package com.haizhi.iap.follow.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Map;

/**
* @description 风险和机会事件消息
* @author LewisLouis
* @date 2018/8/31
*/
@Data
public class NotifyEventInfo {

    /**
     * 公司名称
     */
    private String company;

    /**
     * 事件消息等级（"高"、"中"、"低"）
     */
    private String level;

    /**
     * 事件详情
     */
    @JsonIgnore
    private String detail;

    /**
     * 细节
     */
    private Map<String,Object> details;

    /**
     * 事件类型（0:）
     */
    private Integer ruleType;

    /**
     * 标题
     */
    private String title;

    /**
     * 消息类型
     */
    private Integer type;

    /**
     * 消息生成时间
     */
    private String pushTime;


    String subTypeCnName;


    String subTypeEnName;

    /**
     * 规则类型中文名称：风险、营销
     */
    String typeCnName;


    /**
     * 规则类型英文名称：marketing、risk
     */
    String typeEnName;

}
