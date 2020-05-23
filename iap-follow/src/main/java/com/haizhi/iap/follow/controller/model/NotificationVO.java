package com.haizhi.iap.follow.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.haizhi.iap.follow.enums.NotificationType;
import com.haizhi.iap.follow.model.notification.Notification;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class NotificationVO {
    public static final String marketing_en_name = "marketing";
    public static final String marketing_cn_name = "营销";

    public static final String risk_en_name = "risk";
    public static final String risk_cn_name = "风险";

    Long id;

    @JsonProperty("user_id")
    Long userId;

    String company;

    @JsonProperty("master_company")
    String masterCompany;

    String title;

    @JsonProperty("type_cn_name")
    String typeCnName;

    @JsonProperty("type_en_name")
    String typeEnName;

    String level;

    @JsonProperty("rule_name")
    String ruleName;

    Integer read;

    Integer collected;

    Map<String, Object> detail;

    @JsonProperty("push_time")
    Date pushTime;

    @JsonProperty("sub_type_cn_name")
    String subTypeCnName;

    @JsonProperty("sub_type_en_name")
    String subTypeEnName;

    String role;

    String desc;

    @JsonProperty("litigant_list_other")
    List<String> litigantList;

    public NotificationVO(Notification notification) {
        this.id = notification.getId();
        this.userId = notification.getUserId();
        this.company = notification.getCompany();
        this.masterCompany = notification.getMasterCompany();
        this.title = notification.getTitle();
        this.read = notification.getRead();
        this.detail = notification.getDetail();
        this.collected = notification.getCollected();
        this.pushTime = notification.getPushTime();
        this.level = notification.getLevel();
        this.ruleName = notification.getRuleName();
        this.role = notification.getRole();
        this.desc = notification.getDesc();
        this.litigantList = notification.getLitigantList();

        NotificationType notificationType = NotificationType.get(notification.getType());
        if (notificationType != null) {
            this.subTypeCnName = notificationType.getCnName();
            this.subTypeEnName = notificationType.getEnName();
        }

        int type = notification.getType();
        if (type < 200) {
            this.typeCnName = marketing_cn_name;
            this.typeEnName = marketing_en_name;
        } else {
            this.typeCnName = risk_cn_name;
            this.typeEnName = risk_en_name;
        }
    }
}
