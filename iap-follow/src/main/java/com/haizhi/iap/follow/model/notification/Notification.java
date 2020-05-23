package com.haizhi.iap.follow.model.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.haizhi.iap.follow.utils.DateUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by chenbo on 17/5/3.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Notification {

    Long id;

    @JsonProperty("user_id")
    Long userId;

    String company;

    String masterCompany;

    String title;

    String level;

    String desc;

    @JsonProperty("rule_name")
    String ruleName;

    Integer type;

    @JsonProperty("type_cn_name")
    String typeCnName;

    @JsonProperty("type_en_name")
    String typeEnName;

    Integer read;

    Integer collected;

    @JsonProperty("push_time")
    Date pushTime;

    Boolean isClosely;

    @JsonProperty("sub_type_cn_name")
    String subTypeCnName;

    @JsonProperty("sub_type_en_name")
    String subTypeEnName;

    Map<String, Object> detail;

    String date;

    String role;

    @JsonProperty("litigant_list_other")
    List<String> litigantList;

    public String getDate(){
        return DateUtils.format(getPushTime(), DateUtils.FORMAT_DAY);
    }

    abstract public int getType();

}
