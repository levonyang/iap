package com.haizhi.iap.search.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

/**
 * Created by chenbo on 2017/8/24.
 */
@Data
public class ClusterDomain {

    Long id;

    @JsonProperty("domain_name")
    String domainName;

    String type;

    @JsonProperty("create_time")
    Date createTime;

    @JsonProperty("update_time")
    Date updateTime;
}
