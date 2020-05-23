package com.haizhi.iap.follow.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by chenbo on 17/1/11.
 */
@Data
@NoArgsConstructor
public class TaskView {
    // 如果是没有task_name，则必须有company_name
    @Setter
    @JsonProperty("task_name")
    String taskName;
    @Setter
    @JsonProperty("follow_list_id")
    Long followListId;
    @Setter
    @JsonProperty("data_type")
    String dataType;
    @Setter
    @JsonProperty("begin_date")
    Long beginDate;
    @Setter
    @JsonProperty("end_date")
    Long endDate;
    @Setter
    @JsonProperty("time_option")
    String timeOption;

    // 导出类型 PDF_report or EXCEL  2.0新增
    @Setter
    @JsonProperty("type")
    String type;

    // 是否是单家导出 2.0新增
    @Setter
    @JsonProperty("is_single")
    Boolean isSingle;

    // 公司名，如果是单家报告导出，则需要公司名 2.0新增
    @Setter
    @JsonProperty("company_names")
    String companyNames;
    @Setter
    @JsonProperty("expire_days")
    Long expireDays;
    @Setter
    String mode;
    @Setter
    String method;
    @Setter
    String imgIntroListString;  //以*分割
    @Setter
    String imgPathListString;   //以*分割
}
