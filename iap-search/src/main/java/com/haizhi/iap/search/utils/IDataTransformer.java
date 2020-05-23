package com.haizhi.iap.search.utils;

import com.google.common.collect.Maps;
import com.haizhi.iap.search.controller.model.GraphVo;
import com.haizhi.iap.search.controller.model.Tree;
import com.haizhi.iap.search.enums.GraphField;
import com.haizhi.iap.search.model.GraphEdge;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by thomas on 18/3/21.
 */
public interface IDataTransformer
{
    /**
     * 图转树
     * 默认实现：relations是一个List&lt;Map&lt;String, Object&gt;&gt;
     *
     * @param graph
     * @return
     */
    default Tree graph2Tree(GraphVo graph) {
        return graph2Tree(graph,GraphEdge.Direction.IN);
    }

    default Tree graph2Tree(GraphVo graph, GraphEdge.Direction direction) {
        if(graph == null || CollectionUtils.isEmpty(graph.getVertexes())) return null;
        Tree<List<Map<String, Object>>> root = new Tree<>(graph.getVertexes().get(0));
        String id = graph.getVertexes().get(0).get("_id").toString();
        if(!CollectionUtils.isEmpty(graph.getEdges()))
        {
            //<vertexId, Tree>
            Map<String, Tree<List<Map<String, Object>>>> idTreeMap = Maps.newHashMap();
            idTreeMap.putIfAbsent(id, root);

            graph.getVertexes().stream().filter(vertex -> !CollectionUtils.isEmpty(vertex)).distinct().forEach(vertex -> {
                idTreeMap.putIfAbsent(vertex.get("_id").toString(), new Tree<>(vertex));
            });

            graph.getEdges().stream().filter(edge -> !CollectionUtils.isEmpty(edge)).distinct().forEach(edge -> {
                Tree<List<Map<String, Object>>> parent = idTreeMap.get(findParentKey(edge,direction));
                Tree<List<Map<String, Object>>> child = idTreeMap.get(findChildKey(edge,direction));
                if (parent == null || child == null) return;
                if (CollectionUtils.isEmpty(parent.getChildren()))
                    parent.setChildren(new ArrayList<>());
                parent.getChildren().add(child);
                if (CollectionUtils.isEmpty(child.getRelations()))
                    child.setRelations(new ArrayList<>());
                child.getRelations().add(edge);
            });
        }
        return root;
    }

    default String findParentKey(Map<String,Object> edge, GraphEdge.Direction direction){
        return GraphEdge.Direction.ALL == direction || GraphEdge.Direction.IN == direction ?edge.get(GraphField._TO.getName()).toString():edge.get(GraphField._FROM.getName()).toString();
    }

    default String findChildKey(Map<String,Object> edge, GraphEdge.Direction direction){
        return GraphEdge.Direction.ALL == direction || GraphEdge.Direction.IN == direction ?edge.get(GraphField._FROM.getName()).toString():edge.get(GraphField._TO.getName()).toString();
    }

    /**
     * 对图数据进行一些处理
     *
     * @param graph
     * @return
     */
    default GraphVo processGraph(GraphVo graph)
    {
        return graph;
    }
}
