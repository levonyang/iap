package com.haizhi.iap.search.utils;

import com.google.common.collect.Maps;
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
public class EventDataTransformer implements IDataTransformer
{
    public static final String PARTY_A = "甲方";
    public static final String PARTY_B = "乙方";
    public static final String SUE = "起诉";
    public static final String BE_SUED = "被起诉";
    public static final String PLANTIFF = "同为原告";
    public static final String DEFENDANT = "同为被告";
    public static final String MENTIONED_TOGETHER = "共同提及";

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

            //甲方
            List<Tree<List<Map<String, Object>>>> partyAChildren = edges.stream().filter(edge -> {
                return EdgeCollection.PARTY_BID.getTableName().equals(StringUtils.substringBefore(edge.get("_id").toString(), "/")) && centralVertex.get("_id").toString().equals(edge.get("_from"));
            }).map(edge -> {
                Tree<List<Map<String, Object>>> child = idTreeMap.get(edge.get("_to").toString());
                if (child != null && CollectionUtils.isEmpty(child.getRelations()))
                {
                    child.setRelations(new ArrayList<>());
                    child.getRelations().add(edge);
                }
                return child;
            }).filter(Objects::nonNull).collect(Collectors.toList());

            //乙方
            List<Tree<List<Map<String, Object>>>> partyBChildren = edges.stream().filter(edge -> {
                return EdgeCollection.PARTY_BID.getTableName().equals(StringUtils.substringBefore(edge.get("_id").toString(), "/")) && centralVertex.get("_id").toString().equals(edge.get("_to"));
            }).map(edge -> {
                Tree<List<Map<String, Object>>> child = idTreeMap.get(edge.get("_from").toString());
                if (child != null && CollectionUtils.isEmpty(child.getRelations()))
                {
                    child.setRelations(new ArrayList<>());
                    child.getRelations().add(edge);
                }
                return child;
            }).filter(Objects::nonNull).collect(Collectors.toList());

            //起诉
            List<Tree<List<Map<String, Object>>>> sueChildren = edges.stream().filter(edge -> {
                return EdgeCollection.SUE_RELATE.getTableName().equals(StringUtils.substringBefore(edge.get("_id").toString(), "/")) && centralVertex.get("_id").toString().equals(edge.get("_from"));
            }).map(edge -> {
                Tree<List<Map<String, Object>>> child = idTreeMap.get(edge.get("_to").toString());
                if (child != null && CollectionUtils.isEmpty(child.getRelations()))
                {
                    child.setRelations(new ArrayList<>());
                    child.getRelations().add(edge);
                }
                return child;
            }).filter(Objects::nonNull).collect(Collectors.toList());

            //被诉
            List<Tree<List<Map<String, Object>>>> beSuedChildren = edges.stream().filter(edge -> {
                return EdgeCollection.SUE_RELATE.getTableName().equals(StringUtils.substringBefore(edge.get("_id").toString(), "/")) && centralVertex.get("_id").toString().equals(edge.get("_to"));
            }).map(edge -> {
                Tree<List<Map<String, Object>>> child = idTreeMap.get(edge.get("_from").toString());
                if (child != null && CollectionUtils.isEmpty(child.getRelations()))
                {
                    child.setRelations(new ArrayList<>());
                    child.getRelations().add(edge);
                }
                return child;
            }).filter(Objects::nonNull).collect(Collectors.toList());

            //同为原告
            List<Tree<List<Map<String, Object>>>> plaintiffChildren = edges.stream().filter(edge -> {
                return EdgeCollection.PLAINTIFF_RELATE.getTableName().equals(StringUtils.substringBefore(edge.get("_id").toString(), "/"));
            }).map(edge -> {
                Tree<List<Map<String, Object>>> child = edge.get("_from").toString().equals(centralVertex.get("_id").toString()) ? idTreeMap.get(edge.get("_to").toString()) : idTreeMap.get(edge.get("_from").toString());
                if (child != null && CollectionUtils.isEmpty(child.getRelations()))
                {
                    child.setRelations(new ArrayList<>());
                    child.getRelations().add(edge);
                }
                return child;
            }).filter(Objects::nonNull).collect(Collectors.toList());

            //同为被告
            List<Tree<List<Map<String, Object>>>> defendantChildren = edges.stream().filter(edge -> {
                return EdgeCollection.DEFENDANT_RELATE.getTableName().equals(StringUtils.substringBefore(edge.get("_id").toString(), "/"));
            }).map(edge -> {
                Tree<List<Map<String, Object>>> child = edge.get("_from").toString().equals(centralVertex.get("_id").toString()) ? idTreeMap.get(edge.get("_to").toString()) : idTreeMap.get(edge.get("_from").toString());
                if (child != null && CollectionUtils.isEmpty(child.getRelations()))
                {
                    child.setRelations(new ArrayList<>());
                    child.getRelations().add(edge);
                }
                return child;
            }).filter(Objects::nonNull).collect(Collectors.toList());

            //共同提及
            List<Tree<List<Map<String, Object>>>> mentionedTogetherChildren = edges.stream().filter(edge -> {
                return EdgeCollection.NEWS_ENTITY_RELATE.getTableName().equals(StringUtils.substringBefore(edge.get("_id").toString(), "/"));
            }).map(edge -> {
                Tree<List<Map<String, Object>>> child = edge.get("_from").toString().equals(centralVertex.get("_id").toString()) ? idTreeMap.get(edge.get("_to").toString()) : idTreeMap.get(edge.get("_from").toString());
                if (child != null && CollectionUtils.isEmpty(child.getRelations()))
                {
                    child.setRelations(new ArrayList<>());
                    child.getRelations().add(edge);
                }
                return child;
            }).filter(Objects::nonNull).collect(Collectors.toList());

            Stream.of(Pair.of(partyAChildren, PARTY_A), Pair.of(partyBChildren, PARTY_B), Pair.of(sueChildren, SUE), Pair.of(beSuedChildren, BE_SUED),
                    Pair.of(plaintiffChildren, PLANTIFF), Pair.of(defendantChildren, DEFENDANT), Pair.of(mentionedTogetherChildren, MENTIONED_TOGETHER))
                    .forEach(pair -> {
                        List<Tree<List<Map<String, Object>>>> children = pair.getLeft();
                        //create a virtual tree node
                        Tree<List<Map<String, Object>>> virtualTree = new Tree<>(Collections.singletonMap("name", pair.getRight()));
                        if(CollectionUtils.isEmpty(root.getChildren()))
                            root.setChildren(new ArrayList<>());
                        root.getChildren().add(virtualTree);

                        //add all children under the virtual tree node
                        if(!CollectionUtils.isEmpty(children)) {
                            List<Tree<List<Map<String, Object>>>> trees = children.stream().map(tree -> {
                                Tree<List<Map<String, Object>>> newTree = new Tree(tree.getProperties(), tree.getRelations());
                                return newTree;
                            }).collect(Collectors.toList());
                            virtualTree.setChildren(trees);
                        }
                    });
        }
        return root;
    }
}
