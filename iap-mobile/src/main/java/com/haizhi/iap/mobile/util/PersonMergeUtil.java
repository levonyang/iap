package com.haizhi.iap.mobile.util;

import com.haizhi.iap.mobile.bean.normal.GraphEdge;
import com.haizhi.iap.mobile.bean.result.Graph2;
import com.haizhi.iap.mobile.conf.ArangoSchemaConstants;
import com.haizhi.iap.mobile.repo.GraphRepo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by thomas on 18/3/27.
 */
@Component
public class PersonMergeUtil
{
    @Autowired
    private GraphRepo graphRepo;

    /**
     * 合并多个“_id不同，但实际上为同一人”的自然人
     *
     * @param graph 要进行自然人融合的图数据
     * @return Graph2 融合自然人之后的图数据
     */
    public Graph2 mergePerson(Graph2 graph)
    {
        if(graph == null || CollectionUtils.isEmpty(graph.getVertexes())) return graph;

        Map<String, Map<String, Object>> idPersonMap = new HashMap<>();
        graph.getVertexes().stream().filter(vertex -> {
            return !CollectionUtils.isEmpty(vertex) && ArangoSchemaConstants.TABLE_PERSON.equals(StringUtils.substringBefore(vertex.get("_id").toString(), "/"));
        }).forEach(person -> idPersonMap.putIfAbsent(person.get("_id").toString(), person));

        List<Map<String, Object>> otherVertices = graph.getVertexes().stream().filter(vertex -> {
            return !CollectionUtils.isEmpty(vertex) && !ArangoSchemaConstants.TABLE_PERSON.equals(StringUtils.substringBefore(vertex.get("_id").toString(), "/"));
        }).collect(Collectors.toList());

        List<Map<String, Object>> mergedPersonEdges = findMergedPersonEdges(idPersonMap.keySet());
        Map<String, Set<String>> mergedPersonMap = new HashMap<>();
        List<String> personIds = new ArrayList<>(idPersonMap.keySet());
        for(int i = 0; i < personIds.size(); ++i)
        {
            String personId = personIds.get(i);
            //所有可融合自然人顶点都指向一个中心自然人顶点，找出这个中心顶点
            Map<String, Object> personMergeEdge = mergedPersonEdges.stream().filter(edge -> personId.equals(edge.get("_from").toString())).findAny().orElse(null);
            String centralMergePersonId = personMergeEdge == null ? personId : personMergeEdge.get("_to").toString();

            //与personId属于同一自然人的其他person id
            Set<String> samePersonIds = mergedPersonEdges.stream().filter(edge -> {
                return !personId.equals(edge.get("_from").toString()) && centralMergePersonId.equals(edge.get("_to").toString());
            }).map(edge -> edge.get("_from").toString()).collect(Collectors.toSet());
            if(!centralMergePersonId.equals(personId)) samePersonIds.add(centralMergePersonId);
            //融合后，其他person都会被删除，只保存一个
            samePersonIds.forEach(id -> {
                Map<String, Object> remove = idPersonMap.remove(id);
                if(remove != null)
                    personIds.remove(remove.get("_id").toString());
            });

            //centralMergePersonId可能没有出现在graph参数中，因此仍以personId为key
            Set<String> set = mergedPersonMap.computeIfAbsent(personId, k -> new HashSet<>());
            //存储“同一自然人”的person ids，以便后续修改边的_from/_to
            set.addAll(samePersonIds);
        }

        if(!CollectionUtils.isEmpty(mergedPersonMap))
        {
            idPersonMap.values().forEach(person -> {
                for (Iterator<Map.Entry<String, Set<String>>> iterator = mergedPersonMap.entrySet().iterator(); iterator.hasNext();)
                {
                    Map.Entry<String, Set<String>> entry = iterator.next();
                    //修改相应的边
                    graph.getEdges().forEach(edge -> {
                        if(entry.getValue().contains(edge.get("_from").toString())) edge.put("_from", entry.getKey());
                        if(entry.getValue().contains(edge.get("_to").toString())) edge.put("_to", entry.getKey());
                    });
                    iterator.remove();
                }
            });
        }
        otherVertices.addAll(idPersonMap.values());
        return new Graph2(otherVertices, graph.getEdges());
    }

    /**
     * 根据给定的personIds，找到person_merge边
     *
     * @param personIds
     * @return
     */
    public List<Map<String, Object>> findMergedPersonEdges(Set<String> personIds)
    {
        List<Map<String, Map<String, Object>>> mergedPersons = graphRepo.findMergedPersons(personIds);
        //<personId, Set<actuallySamePersonId>>
        return mergedPersons.stream().map(mergedPerson -> mergedPerson.get("edges")).distinct().collect(Collectors.toList());
    }


    /**
     * Person相关的图谱展开。展开过程中，会把“_id不同，但实际上为同一人”的自然人找出来一起展开
     *
     * @return
     */
    public List<Map<String, Map<String, Object>>> expandWithPersonMerge(final String withDocuments, Collection<String> edgeTables, final int depth, Set<String> startVertices, GraphEdge.Direction direction, String filterStr, Map<String, Object> params, Integer offset, Integer size)
    {
        if(CollectionUtils.isEmpty(edgeTables)) return Collections.emptyList();
        List<Map<String, Map<String, Object>>> results = new ArrayList<>();

        int i = 0;
        Set<String> personIds = startVertices.stream().filter(vertex -> ArangoSchemaConstants.TABLE_PERSON.equals(StringUtils.substringBefore(vertex, "/"))).collect(Collectors.toSet());
        if(!CollectionUtils.isEmpty(personIds))
        {
            List<Map<String, Object>> mergedPersonEdges = findMergedPersonEdges(personIds);
            personIds = mergedPersonEdges.stream().flatMap(edge -> Stream.of(edge.get("_from").toString(), edge.get("_to").toString()).distinct()).collect(Collectors.toSet());
            startVertices.addAll(personIds);
        }
        while (!CollectionUtils.isEmpty(startVertices) && i++ < depth)
        {
            List<Map<String, Map<String, Object>>> result = graphRepo.traversalGraphWithParams(withDocuments, edgeTables, 1, startVertices, direction, filterStr, params, offset, size);
            if(!CollectionUtils.isEmpty(result)) results.addAll(result);

            startVertices = result.stream().map(map -> map.get("vertexes")).filter(vertex -> !CollectionUtils.isEmpty(vertex)).map(vertex -> vertex.get("_id").toString()).distinct().collect(Collectors.toSet());
            personIds = startVertices.stream().filter(vertexId -> ArangoSchemaConstants.TABLE_PERSON.equals(StringUtils.substringBefore(vertexId, "/"))).collect(Collectors.toSet());
            if(!CollectionUtils.isEmpty(personIds))
            {
                List<Map<String, Object>> mergedPersonEdges = findMergedPersonEdges(personIds);
                personIds = mergedPersonEdges.stream().flatMap(edge -> Stream.of(edge.get("_from").toString(), edge.get("_to").toString()).distinct()).collect(Collectors.toSet());
                startVertices.addAll(personIds);
            }
        }
        return results;
    }
}
