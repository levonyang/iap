package com.haizhi.iap.search.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by chenbo on 2017/8/24.
 */
@Data
public class ClusterGroup {
    Long id;

    @JsonProperty("domain_id")
    Long domainId;

    @JsonProperty("cluster_name")
    String clusterName;

    @JsonProperty("cluster_cid")
    String clusterCid;

    String paths;

    List<Map<String, Object>> vertexes;

    @JsonProperty("path_count")
    Integer pathCount;

    String type;

    @JsonProperty("create_time")
    Date createTime;

    @JsonProperty("update_time")
    Date updateTime;
}
