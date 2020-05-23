package com.haizhi.iap.tag.param;

import com.haizhi.iap.tag.model.TagCollection;
import lombok.Builder;
import lombok.Data;

public class TagCollectionQueryRequest {
    TagCollection tagCollection;
    Integer from;
    Integer size;

    public TagCollection getTagCollection() {
        return tagCollection;
    }

    public void setTagCollection(TagCollection tagCollection) {
        this.tagCollection = tagCollection;
    }

    public Integer getFrom() {
        return from;
    }

    public void setFrom(Integer from) {
        this.from = from;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}
