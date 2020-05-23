package com.haizhi.iap.search.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

/**
 * Created by chenbo on 2017/8/28.
 */
@Data
public class EntityPathMap {

    Long id;

    @JsonProperty("domain_id")
    Long domainId;

    @JsonProperty("entity_id")
    String entityId;

    @JsonProperty("path_id")
    String pathId;

    @JsonProperty("create_time")
    Date createTime;

    @JsonProperty("update_time")
    Date updateTime;
}
