package com.haizhi.iap.search.model;

import lombok.Data;

import java.util.Map;
import java.util.Set;

@Data
public class TraversalMeta {

    private Set<GraphVertex> vertexSet;
    private Map<GraphVertex, GraphTraceInfo> traceInfoMap;
}
