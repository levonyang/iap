package com.haizhi.iap.search.model.qo;

import com.haizhi.iap.search.constant.EntityType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

/**
* @description
* @author liulu
* @date 2018/12/19
*/
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value="查询族谱成员信息条件", description="用于表示查询族谱成员信息条件")
public class GroupMembersSearchQo {

    @ApiModelProperty(value = "族谱名称", example = "中国烟草总公司")
    @NotBlank(message = "族谱名称不能为空")
    private String groupName;

    @ApiModelProperty(value = "族谱类型" +
            "profile_enterprise_info 关联集团" +
            "market_updown_info  上下游 " +
            "risk_propagation  风险传导 " +
            "risk_guarantee_info 关联担保 " +
            "risk_black_info  黑名单 ", example = "profile_enterprise_info")
    @NotBlank(message = "族谱类型不能为空")
    private String groupType;

    @ApiModelProperty(value = "实体类型，COMPANY：企业，PERSON：个人")
    private EntityType entityType;


    @ApiModelProperty(value = "是否是行内客户,1:行内，0:行外", example = "1")
    private Integer belongInner;



}
