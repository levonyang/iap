package com.haizhi.iap.follow.controller.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
* @description 根据公司列表查询机会或风险事件信息
* @author LewisLouis
* @date 2018/9/26
*/
@Data
@NoArgsConstructor
@ApiModel(description= "根据公司列表查询机会或风险事件信息的请求对象")
public class CompanyEventSearchReq {

    @ApiModelProperty(value = "公司名称列表")
    List<String> companies;

    @ApiModelProperty(value = "事件类型 0：风险； 1：机会")
    Integer ruleType;

    @ApiModelProperty(value = "数据偏移")
    Integer offset;

    @ApiModelProperty(value = "查询数量")
    Integer count;
}
