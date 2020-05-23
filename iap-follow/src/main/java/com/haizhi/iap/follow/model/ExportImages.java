package com.haizhi.iap.follow.model;

/**
 * Created by zhutianpeng on 17/10/14.
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Created by chenbo on 17/1/11.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportImages {
    Long id;

    @JsonProperty("task_id")
    Long taskId;

    String company;

    @JsonProperty("create_time")
    Date createTime;

    @JsonProperty("update_time")
    Date updateTime;

    @JsonProperty("img_path_list")
    String imgPathList;

    @JsonProperty("img_intro_list")
    String imgIntroList;
}

