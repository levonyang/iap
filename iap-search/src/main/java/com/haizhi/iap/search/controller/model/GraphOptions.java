package com.haizhi.iap.search.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by chenbo on 17/2/23.
 */
@Data
@NoArgsConstructor
public class GraphOptions implements Cloneable {

    List<EdgesOption> edges;

    List<String> exclude;

    List<String> expand;

    GraphFilter filter;

    @JsonProperty("max_length")
    Integer maxLength;

    @JsonProperty("min_weight")
    Integer minWeight;

    List<String> ids;

    @Override
    protected Object clone() {
        GraphOptions options = null;
        try{
            options = (GraphOptions) super.clone();
        }catch (CloneNotSupportedException e){
            e.printStackTrace();
        }
        options.setFilter((GraphFilter) filter.clone());
        List<EdgesOption> newEdges = Lists.newArrayList();

        if(edges != null){
            newEdges.addAll(edges.stream().map(option -> (EdgesOption) option.clone()).collect(Collectors.toList()));
        }
        options.setEdges(newEdges);
        return options;
    }
}
