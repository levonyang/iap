package com.haizhi.iap.tag.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @Author dmy
 * @Date 2017/11/2 下午8:41.
 */
@Data
@Builder
public class TagInfo {
    private Integer id;

    private Integer start;

    private Integer end;

    private String name;

    @JsonProperty("tag_type")
    private String tagType;

    @JsonProperty("field_name")
    private String fieldName;   //对应es的filename

    @JsonProperty("parent_ids")
    private List<Integer> parentIds;

    @JsonProperty("parent_names")
    private List<String> parentNames;

    @JsonProperty("parent_field_names")
    private List<String> parentFieldNames;
}
