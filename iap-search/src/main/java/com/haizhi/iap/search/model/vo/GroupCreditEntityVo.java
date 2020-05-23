package com.haizhi.iap.search.model.vo;


import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author weimin
 * @description 集团授信实体
 * @date 2018-12-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class GroupCreditEntityVo {

//    @BsonId
//    @ApiModelProperty("id")
//    private ObjectId id;

    @BsonProperty("cust_id")
    @ApiModelProperty("客户标识")
    private String custId;

    @BsonProperty("cust_name")
    @ApiModelProperty("客户中文全称")
    private String custName;

    @BsonProperty("cert_type")
    @ApiModelProperty("证件类型")
    private String certType;

    @BsonProperty("cert_id")
    @ApiModelProperty("证件号码")
    private String certId;

    @BsonProperty("credit_contract_no")
    @ApiModelProperty("授信合同标识")
    private String creditContractNo;

    @BsonProperty("credit_type_no")
    @ApiModelProperty("授信类型代码")
    private String creditTypeNo;

    @BsonProperty("credit_limit")
    @ApiModelProperty("授信额度")
    private Double creditLimit;

    @BsonProperty("credit_due_date")
    @ApiModelProperty("授信到期日期")
    private String creditDueDate;

    @BsonProperty("credit_status")
    @ApiModelProperty("授信状态")
    private String creditStatus;

    @BsonProperty("credit_cd")
    @ApiModelProperty("授信币种代码")
    private String creditCd;
}
