package com.haizhi.iap.search.model;

import lombok.Data;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Map;

@Data
public class GraphEdge extends GraphVertex {
    public enum Direction {
        ALL("ANY"), IN("INBOUND"), OUT("OUTBOUND");

        private Direction(String marker) {
            this.arangoMarker = marker;
        }

        private String arangoMarker;

        public String getArangoMarker() {
            return arangoMarker;
        }

        @Override
        public String toString() {
            return this.getArangoMarker();
        }
    }

    public GraphEdge(Map<String, Object> data) {
        super(data);
        this.to = (String) data.get("_to");
        this.from = (String) data.get("_from");
    }

    private String to;
    private String from;

    public int hashCode() {
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
        hashCodeBuilder.append(id);
        return hashCodeBuilder.toHashCode();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (o.getClass() != getClass()) return false;
        GraphEdge other = (GraphEdge) o;
        return id == other.getId();
    }

}
