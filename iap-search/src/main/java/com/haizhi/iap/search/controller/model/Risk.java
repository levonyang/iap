package com.haizhi.iap.search.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by chenbo on 17/2/15.
 */
@Data
@NoArgsConstructor
public class Risk {
    /**
     * 开庭公告
     */
    @JsonProperty("court_ktgg")
    DataItem courtSessionAnn;

    /**
     * 法院公告
     */
    @JsonProperty("court_fygg")
    DataItem courtAnn;

    /**
     * 纳税等级
     */
//    @JsonProperty("enterprise_tax_rank")
//    DataItem taxRank;

    /**
     * 审判流程
     */
    @JsonProperty("judge_process")
    DataItem judgeProcess;

    /**
     * 法院判决
     */
    @JsonProperty("judgement_wenshu")
    DataItem judgement;

    /**
     * 欠税公告
     */
    @JsonProperty("owing_tax")
    DataItem owingTax;

    /**
     * 行政处罚
     */
    @JsonProperty("penalty")
    DataItem govPenalty;

    /**
     * 失信人信息
     */
    @JsonProperty("shixin_info")
    DataItem dishonestInfo;

    /**
     * 被执行人信息
     */
    @JsonProperty("zhixing_info")
    DataItem executionInfo;

    /**
     * 海关行政处罚
     */
    @JsonProperty("customs_penalty")
    DataItem customsPenalty;

    /**
     * 排污费征收公告
     */
    @JsonProperty("environment_protection")
    DataItem environmentProtection;
}
