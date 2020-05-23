package com.haizhi.iap.follow.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

/**
 * Created by chenbo on 17/1/12.
 */
@Data
public class Task {

    Long id;

    String name;

    @JsonProperty("user_id")
    String userId;

    @JsonProperty("follow_list_id")
    Long followListId;

    @JsonProperty("data_type")
    String dataType;

    /**
     * 0 on 1 off
     */
    Integer mode;

    @JsonProperty("begin_date")
    Date beginDate;

    @JsonProperty("end_date")
    Date endDate;

    @JsonProperty("expire_days")
    Long expireDays;

    /**
     * 0 in_date 1 data_time
     */
    @JsonProperty("time_option")
    Integer timeOption;

    @JsonProperty("create_time")
    Date createTime;

    @JsonProperty("finish_time")
    Date finishTime;

    @JsonProperty("update_time")
    Date updateTime;

    @JsonProperty("export_file")
    String exportFile;

    //文件长度,字节
    @JsonProperty("export_file_length")
    Long exportFileLength;

    //文件大小,单位M
    @JsonProperty("file_size")
    Double fileSize;

    @JsonProperty("file_size_unit")
    String fileSizeUnit;

    Integer status;

    Integer deleted;

    Double percent;

    @JsonProperty("follow_list_name")
    String followListName;

    String type;

//    @JsonProperty("is_single")
//    Boolean isSingle;

    @JsonProperty("company_names")
    String companyNames;
}
