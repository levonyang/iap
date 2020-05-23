package com.haizhi.iap.follow.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Created by chenbo on 17/1/10.
 */
@Data
@NoArgsConstructor
public class FollowItem {

    Long id;

    @JsonProperty("company_name")
    String companyName;

    @JsonProperty("user_id")
    Long userId;

    @JsonProperty("follow_list_id")
    Long followListId;

    @JsonProperty("is_exist_in")
    Integer isExistsIn;

    /**
     * 是否开启风险信息提醒
     */
    @JsonProperty("risk_notify")
    Integer riskNotify;

    /**
     * 是否开启营销信息提醒
     */
    @JsonProperty("marketing_notify")
    Integer marketingNotify;

    @JsonProperty("closely_risk_notify")
    Integer closelyRiskNotify;

    @JsonProperty("closely_marketing_notify")
    Integer closelyMarketingNotify;

    @JsonProperty("closely_rule")
    String closelyRule;

    @JsonProperty("is_follow")
    Integer isFollow;

    @JsonProperty("create_time")
    Date createTime;

    @JsonProperty("update_time")
    Date updateTime;

    public FollowItem(String companyName){
        this.companyName = companyName;
    }
}
