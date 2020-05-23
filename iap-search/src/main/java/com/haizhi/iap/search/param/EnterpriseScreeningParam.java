package com.haizhi.iap.search.param;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 企业筛选条件
 * @author xuguoqin
 * @date 2018/11/2 4:05 PM
 */
@Data
public class EnterpriseScreeningParam {

    /**
     * 集团名
     */
    @JsonProperty("group_name")
    private String groupName;

    /**
     * 企业类型 eg：行内客户 行外客户
     */
    @JsonProperty("enterprise_type")
    private String enterpriseType;

    /**
     * 省份
     */
    private List<String> province;

    /**
     * 城市
     */
    private List<String> city;

    /**
     * 行业列表
     */
    private List<String> industries;

    /**
     * 经营状态
     */
    @JsonProperty("business_status")
    private List<String> businessStatus;

    /**
     * 成立年限
     */
    @JsonProperty("registered_date")
    private Range registeredDate;

    /**
     * 注册资本
     */
    @JsonProperty("registerd_capital")
    private Range registeredCapital;

    /**
     * 是否上市公司 eg: true 是 false 否
     */
    @JsonProperty("is_listed_enterprise")
    private Boolean listedEnterprise;
}
