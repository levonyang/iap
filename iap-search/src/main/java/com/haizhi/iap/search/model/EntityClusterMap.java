package com.haizhi.iap.search.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

/**
 * Created by chenbo on 2017/8/28.
 */
@Data
public class EntityClusterMap {

    Long id;

    @JsonProperty("domain_id")
    Long domainId;

    @JsonProperty("entity_id")
    String entityId;

    @JsonProperty("cluster_cid")
    String clusterCid;

    @JsonProperty("create_time")
    Date createTime;

    @JsonProperty("update_time")
    Date updateTime;
}
