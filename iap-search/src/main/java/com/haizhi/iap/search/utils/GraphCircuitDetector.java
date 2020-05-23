package com.haizhi.iap.search.utils;

import edu.uci.ics.jung.graph.Graph;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * Created by thomas on 18/3/29.
 */
@Component
public class GraphCircuitDetector
{

    /**
     * DFS进行环路检测
     *
     * @param centralVertex
     * @param directedGraph
     */
    public void dfsDetect(Map<String, Object> centralVertex, Graph<Map<String, Object>, Map<String, Object>> directedGraph)
    {
        if(CollectionUtils.isEmpty(centralVertex) || directedGraph == null) return;

        Map<String, Boolean> visited = new HashMap<>();
        Map<String, Boolean> onPath = new HashMap<>();
        directedGraph.getVertices().forEach(vertex -> {
            visited.put(vertex.get("_id").toString(), false);
            onPath.put(vertex.get("_id").toString(), false);
        });

        for (Map<String, Object> vertex : new ArrayList<>(directedGraph.getVertices()))
        {
            if(!CollectionUtils.isEmpty(vertex) && !visited.get(vertex.get("_id").toString()))
                dfs(vertex, directedGraph, visited, onPath);
        }
    }

    /**
     *
     * @param vertex 当前访问的顶点
     * @param directedGraph 无向图
     * @param visited 顶点是否被访问过了
     * @param onPath 顶点是否位于dfs路径上
     * @return
     */
    private void dfs(Map<String, Object> vertex, Graph<Map<String, Object>, Map<String, Object>> directedGraph, Map<String, Boolean> visited, Map<String, Boolean> onPath)
    {
        if(CollectionUtils.isEmpty(vertex)) return;
        //this vertex is now visited
        visited.put(vertex.get("_id").toString(), true);
        //this vertex is now on path
        onPath.put(vertex.get("_id").toString(), true);

        for (Iterator<Map<String, Object>> iterator = new ArrayList<>(directedGraph.getInEdges(vertex)).iterator(); iterator.hasNext();)
        {
            Map<String, Object> inEdge = iterator.next();
            Map<String, Object> predecessor = directedGraph.getSource(inEdge);
            if(CollectionUtils.isEmpty(predecessor)) continue;
            //not visited
            if(!visited.get(predecessor.get("_id").toString()))
                dfs(predecessor, directedGraph, visited, onPath);
            //predecessor is visited and on the history path
            //bingo! loop detected!
            else if(onPath.get(predecessor.get("_id").toString()))
            {
                //break the loop
                directedGraph.removeEdge(inEdge);
                iterator.remove();

                //fork edge and predecessor
                Map<String, Object> newPredecessor = new HashMap<>(predecessor);
                newPredecessor.put("_id", String.format("fork%s-%s", UUID.randomUUID().toString().replace("-", "").toUpperCase(), newPredecessor.get("_id").toString()));
                directedGraph.addVertex(newPredecessor);

                Map<String, Object> newEdge = new HashMap<>(inEdge);
                newEdge.put("_from", newPredecessor.get("_id").toString());
                directedGraph.addEdge(newEdge, newPredecessor, vertex);
                break;
            }
        }
        onPath.put(vertex.get("_id").toString(), false);
    }
}
