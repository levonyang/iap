package com.haizhi.iap.mobile.service;

import com.haizhi.iap.mobile.bean.normal.GraphEdge;
import com.haizhi.iap.mobile.bean.result.Graph2;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by thomas on 18/4/14.
 */
@Service
public class GraphService
{
    /**
     * @author thomas
     * 将traverse操作的结果转换成Graph2结构
     *
     * @param results
     * @param centralVertex 若为null，则表示不把centralVertex添加到Graph2中
     * @return
     */
    public Graph2 buildGraphFromTraverseResult(List<Map<String, Map<String, Object>>> results, Map<String, Object> centralVertex)
    {
        if(CollectionUtils.isEmpty(results)) return new Graph2();
        List<Map<String, Object>> vertexes = results.stream().map(map -> map.get("vertexes")).filter(vertex -> !CollectionUtils.isEmpty(vertex)).distinct().collect(Collectors.toList());
        List<Map<String, Object>> edges = results.stream().map(map -> map.get("edges")).filter(edge -> !CollectionUtils.isEmpty(edge)).distinct().collect(Collectors.toList());
        if(!CollectionUtils.isEmpty(centralVertex) && !CollectionUtils.isEmpty(vertexes))
            vertexes.add(0, centralVertex);
        return new Graph2(vertexes, edges);
    }

    /**
     * 手动limit操作
     *
     * @param results
     * @param offset
     * @param size
     * @return
     */
    public List<Map<String, Map<String, Object>>> limit(List<Map<String, Map<String, Object>>> results, Integer offset, Integer size)
    {
        List<Map<String, Map<String, Object>>> tmp = results;
        //limit
        if(offset != null && offset >= 0 && size != null && size > 0)
            tmp = Optional.ofNullable(results).map(Collection::stream).map(stream -> stream.skip(offset).limit(size).collect(Collectors.toList())).orElse(Collections.emptyList());
        return tmp;
    }

    /**
     * 在graph中找出可达vertex的顶点和边
     *
     * @param vertex
     * @param graph
     * @param direction
     * @return
     */
    public Graph2 reachableGraph(Graph2 graph, Map<String, Object> vertex, GraphEdge.Direction direction)
    {
        if(graph == null || CollectionUtils.isEmpty(graph.getVertexes()) || CollectionUtils.isEmpty(graph.getEdges()))
            return new Graph2();
        if(direction == null || GraphEdge.Direction.ALL.equals(direction)) return graph;

        Map<String, Map<String, Object>> idVertexMap = new HashMap<>();
        graph.getVertexes().forEach(v -> idVertexMap.put(v.get("_id").toString(), v));
        Queue<String> queue = new LinkedList<>();
        queue.offer(vertex.get("_id").toString());
        List<Map<String, Object>> tmpEdges = new ArrayList<>(graph.getEdges());
        List<Map<String, Object>> vertices = new ArrayList<>();
        List<Map<String, Object>> edges = new ArrayList<>();
        switch (direction)
        {
            case IN:
                while (!queue.isEmpty())
                {
                    String id = queue.poll();
                    for(Iterator<Map<String, Object>> iterator = tmpEdges.iterator(); iterator.hasNext();)
                    {
                        Map<String, Object> edge = iterator.next();
                        if(id.equals(edge.get("_to").toString()))
                        {
                            String from = edge.get("_from").toString();
                            queue.offer(from);
                            vertices.add(idVertexMap.get(from));
                            edges.add(edge);
                            iterator.remove();
                        }
                    }
                }
                break;
            case OUT:
                while (!queue.isEmpty())
                {
                    String id = queue.poll();
                    for(Iterator<Map<String, Object>> iterator = tmpEdges.iterator(); iterator.hasNext();)
                    {
                        Map<String, Object> edge = iterator.next();
                        if(id.equals(edge.get("_from").toString()))
                        {
                            String to = edge.get("_to").toString();
                            queue.offer(to);
                            vertices.add(idVertexMap.get(to));
                            edges.add(edge);
                            iterator.remove();
                        }
                    }
                }
                break;
            default:break;
        }
        vertices = vertices.stream().distinct().collect(Collectors.toList());
        if(!CollectionUtils.isEmpty(vertices)) vertices.add(0, vertex);
        return new Graph2(vertices, edges.stream().distinct().collect(Collectors.toList()));
    }
}
