package com.haizhi.iap.search.utils;

import com.haizhi.iap.search.controller.model.GraphVo;
import com.haizhi.iap.search.controller.model.Tree;
import com.haizhi.iap.search.model.GraphEdge;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by thomas on 18/3/19.
 *
 * 股权结构 数据处理接口
 */
@Component
public class StockRightDataTransformer implements IDataTransformer
{
    @Autowired
    private PersonMergeUtil personMergeUtil;

    public static final int MAX_TREE_DEPTH = 3;

    @Override
    public Tree<Map<String, Object>> graph2Tree(GraphVo graphVo, GraphEdge.Direction direction){
        if(graphVo == null || CollectionUtils.isEmpty(graphVo.getVertexes())) return null;

        GraphVo graph = personMergeUtil.mergePerson(graphVo);

        Tree<Map<String, Object>> root = new Tree<>(graph.getVertexes().get(0));
        String id = graph.getVertexes().get(0).get("_id").toString();

        if(!CollectionUtils.isEmpty(graph.getEdges()))
        {
            List<Map<String, Object>> edges = graph.getEdges().stream().filter(edge -> !CollectionUtils.isEmpty(edge)).distinct().collect(Collectors.toList());
            //<vertexId, vertex>
            Map<String, Map<String, Object>> idVertexMap = new HashMap<>();
            idVertexMap.putIfAbsent(id, graph.getVertexes().get(0));
            graph.getVertexes().stream().filter(vertex -> !CollectionUtils.isEmpty(vertex)).distinct().forEach(vertex -> {
                idVertexMap.putIfAbsent(vertex.get("_id").toString(), vertex);
            });

            Queue<Tree<Map<String, Object>>> parents = new LinkedList<>();
            parents.offer(root);

            int depth = 0;
            Tree end = root, nextLineEnd = root;
            while (!CollectionUtils.isEmpty(parents) && depth < MAX_TREE_DEPTH)
            {
                Tree<Map<String, Object>> parent = parents.poll();
                if(parent != null)
                {
                    List<Map<String, Object>> childEdges = edges.stream().filter(edge -> parent.getProperties().get("_id").toString().equals(findParentKey(edge,direction))).distinct().collect(Collectors.toList());

                    //<_from, Set<edge>>
                    Map<String, Set<Map<String, Object>>> edgesWithSameFrom = new HashMap<>();
                    childEdges.forEach(childEdge -> {
                        Set<Map<String, Object>> edgeSet = edgesWithSameFrom.computeIfAbsent(findChildKey(childEdge,direction), k -> new HashSet<>());
                        edgeSet.add(childEdge);
                    });

                    for (Map.Entry<String, Set<Map<String, Object>>> entry : edgesWithSameFrom.entrySet())
                    {
                        String childId = entry.getKey();
                        Set<Map<String, Object>> edgeSet = entry.getValue();

                        Map<String, Object> childVertex = idVertexMap.get(childId);
                        if (!CollectionUtils.isEmpty(childVertex))
                        {
                            Tree<Map<String, Object>> child = new Tree<>(childVertex);
                            if (CollectionUtils.isEmpty(parent.getChildren()))
                                parent.setChildren(new ArrayList<>());
                            parent.getChildren().add(child);

                            if (CollectionUtils.isEmpty(child.getRelations()))
                                child.setRelations(new HashMap<>());
                            edgeSet.forEach(edge -> child.getRelations().putIfAbsent(StringUtils.substringBefore(edge.get("_id").toString(), "/"), edge));

                            parents.offer(child);
                            nextLineEnd = child;
                        }
                    }
                    //树换行
                    if(parent == end)
                    {
                        end = nextLineEnd;
                        ++depth;
                    }
                }
            }
        }
        return root;
    }
}
