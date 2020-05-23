package com.haizhi.iap.search.utils;

import com.google.common.collect.Maps;
import com.haizhi.iap.search.conf.GraphEdgeLabel;
import com.haizhi.iap.search.controller.model.GraphVo;
import com.haizhi.iap.search.controller.model.Tree;
import com.haizhi.iap.search.model.EdgeCollection;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by thomas on 18/3/22.
 */
@Component
public class OfficerDataTransformer implements IDataTransformer
{
    public static final String CENTRAL_OFFICER = "核心高管";
    public static final String CHAIRMAN = "董事";
    public static final String SUPERVISOR = "监事";

    public static final Set<String> CENTRAL_OFFICERS = new HashSet<>();
    public static final Set<String> CHAIRMANS = new HashSet<>();
    public static final Set<String> SUPERVISORS = new HashSet<>();

    static {
        CENTRAL_OFFICERS.add("董事长");
        CENTRAL_OFFICERS.add("总经理");
        CENTRAL_OFFICERS.add("执行董事");
        CENTRAL_OFFICERS.add("法定代表人");

        CHAIRMANS.add("董事");

        SUPERVISORS.add("监事");
    }

    @Autowired
    private PersonMergeUtil personMergeUtil;

    @Override
    public Tree graph2Tree(GraphVo graphVo)
    {
        if(graphVo == null || CollectionUtils.isEmpty(graphVo.getVertexes())) return null;

        GraphVo graph = personMergeUtil.mergePerson(graphVo);

        Map<String, Object> centralVertex = graph.getVertexes().get(0);
        Tree<List<Map<String, Object>>> root = new Tree<>(centralVertex);
        String id = centralVertex.get("_id").toString();
        if(!CollectionUtils.isEmpty(graph.getEdges()))
        {
            //<vertexId, Tree>
            Map<String, Tree<List<Map<String, Object>>>> idTreeMap = Maps.newHashMap();
            idTreeMap.putIfAbsent(id, root);
            graph.getVertexes().stream().filter(vertex -> !CollectionUtils.isEmpty(vertex)).distinct().forEach(vertex -> {
                idTreeMap.putIfAbsent(vertex.get("_id").toString(), new Tree<>(vertex));
            });
            List<Map<String, Object>> edges = graph.getEdges().stream().filter(edge -> !CollectionUtils.isEmpty(edge)).distinct().collect(Collectors.toList());

            //<_from, Set<edge>>
            Map<String, Set<Map<String, Object>>> fromEdgeMap = new HashMap<>();
            edges.forEach(edge -> {
                Set<Map<String, Object>> edgeSet = fromEdgeMap.computeIfAbsent(edge.get("_from").toString(), key -> new HashSet<>());
                edgeSet.add(edge);
            });

            if(!CollectionUtils.isEmpty(fromEdgeMap))
            {
                List<Tree<List<Map<String, Object>>>> centralOfficerChildren = new ArrayList<>();
                List<Tree<List<Map<String, Object>>>> chairmanChildren = new ArrayList<>();
                List<Tree<List<Map<String, Object>>>> supervisorChildren = new ArrayList<>();

                fromEdgeMap.forEach((key, value) -> {
                    List<String> positions = value.stream().map(edge -> edge.getOrDefault(GraphEdgeLabel.EDGE_LABEL_CONF_MAP.get(EdgeCollection.OFFICER.getTableName()).getField(), "").toString()).filter(StringUtils::isNotBlank).collect(Collectors.toList());
                    if (positions.stream().filter(CENTRAL_OFFICERS::contains).findAny().orElse(null) != null)
                    {
                        Tree<List<Map<String, Object>>> child = idTreeMap.get(key);
                        if (child != null && CollectionUtils.isEmpty(child.getRelations()))
                        {
                            child.setRelations(new ArrayList<>());
                            Map<String, Object> edge = value.iterator().next();
                            edge.put("label", StringUtils.join(positions, ", "));
                            edge.put("position", StringUtils.join(positions, ", "));
                            child.getRelations().add(edge);
                        }
                        if (child != null) centralOfficerChildren.add(child);
                    } else if (positions.stream().filter(CHAIRMANS::contains).findAny().orElse(null) != null)
                    {
                        Tree<List<Map<String, Object>>> child = idTreeMap.get(key);
                        if (child != null && CollectionUtils.isEmpty(child.getRelations()))
                        {
                            child.setRelations(new ArrayList<>());
                            Map<String, Object> edge = value.iterator().next();
                            edge.put("label", StringUtils.join(positions, ", "));
                            edge.put("position", StringUtils.join(positions, ", "));
                            child.getRelations().add(edge);
                        }
                        if (child != null) chairmanChildren.add(child);
                    } else if (positions.stream().filter(SUPERVISORS::contains).findAny().orElse(null) != null)
                    {
                        Tree<List<Map<String, Object>>> child = idTreeMap.get(key);
                        if (child != null && CollectionUtils.isEmpty(child.getRelations()))
                        {
                            child.setRelations(new ArrayList<>());
                            Map<String, Object> edge = value.iterator().next();
                            edge.put("label", StringUtils.join(positions, ", "));
                            edge.put("position", StringUtils.join(positions, ", "));
                            child.getRelations().add(edge);
                        }
                        if (child != null) supervisorChildren.add(child);
                    }
                });

                Stream.of(Pair.of(centralOfficerChildren, CENTRAL_OFFICER), Pair.of(chairmanChildren, CHAIRMAN), Pair.of(supervisorChildren, SUPERVISOR)).forEach(pair -> {
                    List<Tree<List<Map<String, Object>>>> children = pair.getLeft();
                    //create a virtual tree node
                    Tree<List<Map<String, Object>>> virtualTree = new Tree<>(Collections.singletonMap("name", pair.getRight()));
                    if(CollectionUtils.isEmpty(root.getChildren()))
                        root.setChildren(new ArrayList<>());
                    root.getChildren().add(virtualTree);

                    //add all children under the virtual tree node
                    if(!CollectionUtils.isEmpty(children)) virtualTree.setChildren(children);
                });
            }
        }
        return root;
    }
}
