package com.haizhi.iap.follow.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Created by chenbo on 17/1/10.
 */
@Data
@NoArgsConstructor
public class CompanyImportAck {

    @JsonProperty("items")
    List<CompanyImportItem> items;

    Long total;

    @JsonProperty("priority_first")
    Integer priorityFirst;

    @JsonProperty("priority_second")
    Integer prioritySecond;

    @JsonProperty("priority_third")
    Integer priorityThird;

    @JsonProperty("crawl_level_count")
    List<Map> crawlLevelCount;

    @JsonProperty("cache_key")
    String cacheKey;

    @JsonProperty("follow_list_id")
    Long followListId;
}
