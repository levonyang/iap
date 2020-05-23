package com.haizhi.iap.search.utils;

import com.google.common.collect.Maps;
import com.haizhi.iap.search.controller.model.GraphVo;
import com.haizhi.iap.search.controller.model.Range;
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
public class InvestGroupDataTransformer implements IDataTransformer
{
    public static final String DIRECT_CONTROL = "直接控股";
    public static final String INDIRECT_CONTROL = "间接控制";
    public static final String MAIN_INVEST = "主要投资";
    public static final String OTHER_INVEST = "其它投资";

    /**
     * 主要投资区间
     */
    public static final Range<Double> MAIN_INVEST_RANGE = new Range<>(20., 50., true, true);
    /**
     * 其它投资区间
     */
    public static final Range<Double> OTHER_INVEST_RANGE = new Range<>(null, 20., false, false);

    @Autowired
    private PersonMergeUtil personMergeUtil;

    @Autowired
    private GraphEdgeLabelUtil graphEdgeLabelUtil;

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
            Map<String, Map<String, Object>> idVertexMap = new HashMap<>();
            graph.getVertexes().stream().filter(vertex -> !CollectionUtils.isEmpty(vertex)).distinct().forEach(vertex -> {
                idTreeMap.putIfAbsent(vertex.get("_id").toString(), new Tree<>(vertex));
                idVertexMap.putIfAbsent(vertex.get("_id").toString(), vertex);
            });

            graphEdgeLabelUtil.makeEdgeLabel(graph.getEdges(), idVertexMap);
            List<Map<String, Object>> edges = graph.getEdges().stream().filter(edge -> !CollectionUtils.isEmpty(edge)).distinct().collect(Collectors.toList());

            //属于直接控制 和 间接控制 的顶点ID
            Set<String> directOrIndirectControlSet = new HashSet<>();
            //直接控制
            List<Tree<List<Map<String, Object>>>> directControlChildren = edges.stream().filter(edge -> EdgeCollection.CONTROL_SHAREHOLDER.getTableName().equals(StringUtils.substringBefore(edge.get("_id").toString(), "/")))
                    .map(edge -> {
                        Tree<List<Map<String, Object>>> child = idTreeMap.get(edge.get("_to").toString());
                        if (child != null && CollectionUtils.isEmpty(child.getRelations()))
                        {
                            child.setRelations(new ArrayList<>());
                            child.getRelations().add(edge);
                            directOrIndirectControlSet.add(edge.get("_to").toString());
                        }
                        return child;
                    }).filter(Objects::nonNull).collect(Collectors.toList());

            //间接控制
            List<Tree<List<Map<String, Object>>>> indirectControlChildren = edges.stream().filter(edge -> EdgeCollection.ACTUAL_CONTROLLER.getTableName().equals(StringUtils.substringBefore(edge.get("_id").toString(), "/")))
                    .map(edge -> {
                        Tree<List<Map<String, Object>>> child = idTreeMap.get(edge.get("_to").toString());
                        if (child != null && CollectionUtils.isEmpty(child.getRelations()))
                        {
                            child.setRelations(new ArrayList<>());
                            child.getRelations().add(edge);
                            directOrIndirectControlSet.add(edge.get("_to").toString());
                        }
                        return child;
                    }).filter(Objects::nonNull).collect(Collectors.toList());

            //投资关系，且_to顶点 不属于直接控制 和 间接控制
            List<Map<String, Object>> investEdges = edges.stream().filter(edge -> {
                return EdgeCollection.INVEST.getTableName().equals(StringUtils.substringBefore(edge.get("_id").toString(), "/")) && !directOrIndirectControlSet.contains(edge.get("_to").toString());
            }).collect(Collectors.toList());

            //主要投资
            List<Tree<List<Map<String, Object>>>> mainShareholderChildren = investEdges.stream().filter(edge -> {
                Double percentage = graphEdgeLabelUtil.getInvestPercentage(edge, idVertexMap);
                return percentage != null && MAIN_INVEST_RANGE.inRange(percentage);
            }).map(edge -> {
                Tree<List<Map<String, Object>>> child = idTreeMap.get(edge.get("_to").toString());
                if (child != null && CollectionUtils.isEmpty(child.getRelations()))
                {
                    child.setRelations(new ArrayList<>());
                    child.getRelations().add(edge);
                }
                return child;
            }).filter(Objects::nonNull).collect(Collectors.toList());

            //其它投资
            List<Tree<List<Map<String, Object>>>> otherInvestChildren = investEdges.stream().filter(edge -> {
                Double percentage = graphEdgeLabelUtil.getInvestPercentage(edge, idVertexMap);
                return percentage == null || OTHER_INVEST_RANGE.inRange(percentage);
            }).map(edge -> {
                Tree<List<Map<String, Object>>> child = idTreeMap.get(edge.get("_to").toString());
                if (child != null && CollectionUtils.isEmpty(child.getRelations()))
                {
                    child.setRelations(new ArrayList<>());
                    child.getRelations().add(edge);
                }
                return child;
            }).filter(Objects::nonNull).collect(Collectors.toList());

            Stream.of(Pair.of(directControlChildren, DIRECT_CONTROL), Pair.of(indirectControlChildren, INDIRECT_CONTROL), Pair.of(mainShareholderChildren, MAIN_INVEST), Pair.of(otherInvestChildren, OTHER_INVEST)).forEach(pair -> {
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
        return root;
    }
}
