package com.haizhi.iap.search.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
* @description 单个族谱的所有企业信息
* @author liulu
* @date 2018/12/19
*/
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value="单个族谱的所有企业信息", description="用于表示单个族谱的所有企业信息")
public class GroupCompanyVo {

    @ApiModelProperty(value = "企业列表信息 map为每个成员的属性信息列表")
    List<Map<String,Object>> companies = new ArrayList<>();

    @ApiModelProperty(value = "企业数量",example = "1")
    @JsonProperty("num")
    Integer num;

}
