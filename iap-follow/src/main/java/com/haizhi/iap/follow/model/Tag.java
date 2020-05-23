package com.haizhi.iap.follow.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Created by chenbo on 2017/12/14.
 */
@Data
public class Tag implements Comparable {

    Long id;

    @JsonProperty("parent_id")
    Long parentId;

    @JsonProperty("collection_id")
    Long collectionId;

    String name;

    @JsonProperty("field_name")
    String fieldName;

    @JsonProperty("is_deleted")
    Integer isDeleted;

    Integer level;

    @JsonProperty("is_hot")
    Integer isHot;

    @Override
    public int compareTo(Object o) {
        if(o instanceof Tag){
            return this.name.compareTo(((Tag) o).getName());
        }
        return 1;
    }
}
