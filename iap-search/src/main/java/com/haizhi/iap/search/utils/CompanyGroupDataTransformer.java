package com.haizhi.iap.search.utils;

import com.haizhi.iap.search.controller.model.GraphVo;
import com.haizhi.iap.search.model.VertexCollection;
import edu.uci.ics.jung.algorithms.scoring.PageRank;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraDistance;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by thomas on 18/3/21.
 *
 * 集团派系 数据处理接口
 */
@Component
public class CompanyGroupDataTransformer implements IDataTransformer
{
    /**
     * pagerank算法中的alpha参数
     */
    private static final double ALPHA = 0.85;

    @Autowired
    private PersonMergeUtil personMergeUtil;

    @Autowired
    private GraphEdgeLabelUtil graphEdgeLabelUtil;

    @Override
    public GraphVo processGraph(GraphVo graphVo)
    {
        if(graphVo == null || CollectionUtils.isEmpty(graphVo.getVertexes()) || CollectionUtils.isEmpty(graphVo.getEdges())) return null;

        GraphVo graph = personMergeUtil.mergePerson(graphVo);

        Map<String, Map<String, Object>> idVertexMap = new HashMap<>();
        graph.getVertexes().forEach(vertex -> idVertexMap.putIfAbsent(vertex.get("_id").toString(), vertex));

        Graph<Map<String, Object>, Map<String, Object>> undirectedGraph = UndirectedSparseMultigraph.<Map<String, Object>, Map<String, Object>>getFactory().get();
        graph.getVertexes().forEach(undirectedGraph::addVertex);
        graph.getEdges().forEach(edge -> {
            Map<String, Object> fromVertex = idVertexMap.get(edge.get("_from").toString());
            Map<String, Object> toVertex = idVertexMap.get(edge.get("_to").toString());
            if(fromVertex == null || toVertex == null) return;
            undirectedGraph.addEdge(edge, fromVertex, toVertex);
        });

        //计算其他所有顶点到中心顶点的距离
        DijkstraDistance<Map<String, Object>, Map<String, Object>> dijkstraDistance = new DijkstraDistance<>(undirectedGraph);
        Map<Map<String, Object>, Number> distanceMap = dijkstraDistance.getDistanceMap(graph.getVertexes().get(0));

        //裁剪掉相邻顶点数小于2，且与中心节点直接相连的自然人节点
        List<Map<String, Object>> verticesToRemove = undirectedGraph.getVertices().stream().filter(vertex -> {
            int cnt = undirectedGraph.getSuccessorCount(vertex);
            Number distance = distanceMap.getOrDefault(vertex, 0);
            return cnt < 2 && distance.intValue() == 1 && VertexCollection.PERSON.equals(StringUtils.substringBefore(vertex.get("_id").toString(), "/"));
        }).collect(Collectors.toList());
        while (!CollectionUtils.isEmpty(verticesToRemove))
        {
            for (Map<String, Object> vertex : verticesToRemove)
                undirectedGraph.removeVertex(vertex);
            verticesToRemove = undirectedGraph.getVertices().stream().filter(vertex -> {
                int cnt = undirectedGraph.getSuccessorCount(vertex);
                Number distance = distanceMap.getOrDefault(vertex, 0);
                return cnt < 2 && distance.intValue() == 1 && VertexCollection.PERSON.equals(StringUtils.substringBefore(vertex.get("_id").toString(), "/"));
            }).collect(Collectors.toList());
        }

        //remove orphan vertices
        graph.getVertexes().stream().filter(vertex -> undirectedGraph.containsVertex(vertex) && undirectedGraph.degree(vertex) == 0).forEach(undirectedGraph::removeVertex);

        //计算每个节点的pageRank值
        PageRank<Map<String, Object>, Map<String, Object>> pageRank = new PageRank<>(undirectedGraph, ALPHA);
        pageRank.setTolerance(0.1);
        pageRank.acceptDisconnectedGraph(true);
        try {
            pageRank.evaluate();
        } catch (Exception ignore) {}

        Collection<Map<String, Object>> vertices = undirectedGraph.getVertices();
        Collection<Map<String, Object>> edges = undirectedGraph.getEdges();
        if(!CollectionUtils.isEmpty(vertices))
        {
            vertices.forEach(vertex -> {
                Double pageRankValue = 0.;
                try {
                    pageRankValue = pageRank.getVertexScore(vertex);
                } catch (Exception ignore) {}
                vertex.put("pageRank", pageRankValue);
            });
        }
        graphEdgeLabelUtil.makeEdgeLabel(edges, idVertexMap);
        return new GraphVo(new ArrayList<>(vertices), new ArrayList<>(edges));
    }
}
