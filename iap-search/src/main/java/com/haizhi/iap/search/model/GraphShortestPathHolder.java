package com.haizhi.iap.search.model;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class GraphShortestPathHolder {

    public GraphShortestPathHolder(Set<String> nextStart) {
        this.nextStart = nextStart;
        for (String one : nextStart) {
            vertexes.put(one, Collections.EMPTY_MAP);
            preEdges.put(one, Collections.EMPTY_LIST);
        }
    }

    private Set<String> nextStart;
    private Set<String> personMerge = Sets.newHashSet();

    private Map<String, Map<String, Object>> vertexes = Maps.newHashMap();

    private Map<String, List<Map<String, Object>>> preEdges = Maps.newHashMap();

}
