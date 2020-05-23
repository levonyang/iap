package com.haizhi.iap.search.controller.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by haizhi on 2018/8/17.
 */
@Data
@NoArgsConstructor
@ApiModel(description = "族谱列表分页展示的请求对象")
public class GraphListReq {
    @ApiModelProperty(value = "企业名称",example = "安徽电气集团股份有限公司")
    String entityName;
    @ApiModelProperty(value = "族谱类型",example = "profile_enterprise_info")
    String type;
    @ApiModelProperty(value = "每页条数",example = "10")
    Integer pageSize;
    @ApiModelProperty(value = "偏移量",example = "20")
    Integer offset;
    @ApiModelProperty(value = "排序的字段\"entity_count\"按集团包含的实体数排序，\"inner_entity_count\":按行内客户数排序",
            example = "inner_entity_count")
    String field;
    @ApiModelProperty(value = "顺序\"desc\":倒序，\"asc\":升序",example ="desc")
    String sort;
}