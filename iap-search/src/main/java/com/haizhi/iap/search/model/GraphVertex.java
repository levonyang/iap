package com.haizhi.iap.search.model;

import lombok.Data;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Map;

@Data
public class GraphVertex {

    public GraphVertex(Map<String, Object> data) {
        this.data = data;
        this.id = (String) data.get("_id");
    }

    protected String id;
    private Map<String, Object> data;

    public int hashCode() {
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
        hashCodeBuilder.append(id);
        return hashCodeBuilder.toHashCode();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (o.getClass() != getClass()) return false;
        GraphVertex other = (GraphVertex) o;
        return id == other.getId();
    }
}
