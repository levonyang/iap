package com.haizhi.iap.search.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.haizhi.iap.search.controller.model.GraphVo;
import com.haizhi.iap.search.controller.model.Tree;
import com.haizhi.iap.search.model.VertexCollection;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by thomas on 18/3/21.
 *
 * 资金往来 数据处理接口
 */
@Component
public class MoneyFlowDataTransformer implements IDataTransformer
{
    public static final String OUT_TO_PERSON = "转出至个人";
    public static final String IN_TO_PERSON = "个人转入";
    public static final String OUT_TO_COMPANY = "转出至企业";
    public static final String IN_TO_COMPANY = "企业转入";

    @Autowired
    private PersonMergeUtil personMergeUtil;

    @Override
    public Tree<List<Map<String, Object>>> graph2Tree(GraphVo graphVo)
    {
        if(graphVo == null || CollectionUtils.isEmpty(graphVo.getVertexes())) return null;

        GraphVo graph = personMergeUtil.mergePerson(graphVo);

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
            List<Map<String, Object>> edges = graph.getEdges().stream().filter(edge -> !CollectionUtils.isEmpty(edge)).distinct().collect(Collectors.toList());

            /**
             * add by linyong 2020-04-12
             * 以下在补充idTreeMap里Tree的Relations(分为四种情况，
             * 1.转出到个人，_from为自己;
             * 2.转出至企业，_from为自己;
             * 3.个人转入，_to为自己;
             * 4.企业转入,_to为自己)
             * ，并且最终生成4个资金关系的集合
             */
            //转出至个人
            List<Tree<List<Map<String, Object>>>> outToPersonChildren = edges.stream().filter(edge -> edge.get("_from").toString().equals(id) && edge.get("_to").toString().startsWith(VertexCollection.PERSON))
                    .map(edge -> {
                        Tree<List<Map<String, Object>>> child = idTreeMap.get(edge.get("_to").toString());
                        if (child != null && CollectionUtils.isEmpty(child.getRelations()))
                        {
                            child.setRelations(Lists.newArrayList());
                            child.getRelations().add(edge);
                        }
                        return child;
                    }).filter(Objects::nonNull).collect(Collectors.toList());

            //转出至企业
            List<Tree<List<Map<String, Object>>>> outToCompanyChildren = edges.stream().filter(edge -> edge.get("_from").toString().equals(id) && edge.get("_to").toString().startsWith(VertexCollection.COMPANY))
                    .map(edge -> {
                        Tree<List<Map<String, Object>>> child = idTreeMap.get(edge.get("_to").toString());
                        if (child != null && CollectionUtils.isEmpty(child.getRelations()))
                        {
                            child.setRelations(Lists.newArrayList());
                            child.getRelations().add(edge);
                        }
                        return child;
                    }).filter(Objects::nonNull).collect(Collectors.toList());

            //个人转入
            List<Tree<List<Map<String, Object>>>> intoPersonChildren = edges.stream().filter(edge -> edge.get("_to").toString().equals(id) && edge.get("_from").toString().startsWith(VertexCollection.PERSON))
                    .map(edge -> {
                        Tree<List<Map<String, Object>>> child = idTreeMap.get(edge.get("_from").toString());
                        if (child != null && CollectionUtils.isEmpty(child.getRelations()))
                        {
                            child.setRelations(Lists.newArrayList());
                            child.getRelations().add(edge);
                        }
                        return child;
                    }).filter(Objects::nonNull).collect(Collectors.toList());

            //企业转入
            List<Tree<List<Map<String, Object>>>> intoCompanyChildren = edges.stream().filter(edge -> edge.get("_to").toString().equals(id) && edge.get("_from").toString().startsWith(VertexCollection.COMPANY))
                    .map(edge -> {
                        Tree<List<Map<String, Object>>> child = idTreeMap.get(edge.get("_from").toString());
                        if (child != null && CollectionUtils.isEmpty(child.getRelations()))
                        {
                            child.setRelations(Lists.newArrayList());
                            child.getRelations().add(edge);
                        }
                        return child;
                    }).filter(Objects::nonNull).collect(Collectors.toList());

            Stream.of(Pair.of(outToPersonChildren, OUT_TO_PERSON), Pair.of(outToCompanyChildren, OUT_TO_COMPANY), Pair.of(intoPersonChildren, IN_TO_PERSON), Pair.of(intoCompanyChildren, IN_TO_COMPANY)).forEach(pair -> {
                List<Tree<List<Map<String, Object>>>> children = pair.getLeft();
                //create a virtual tree node
                Tree<List<Map<String, Object>>> virtualTree = new Tree<>(Collections.singletonMap("name", pair.getRight()));
                if(CollectionUtils.isEmpty(root.getChildren()))
                    root.setChildren(new ArrayList<>());
                root.getChildren().add(virtualTree);

                //add all children under the virtual tree node
                if(!CollectionUtils.isEmpty(children)) virtualTree.setChildren(children);
            });
            /**
             * 最终结构 add by linyong 2020-04-12 批注:写这种乱七八糟的结构是要干嘛我也不太懂，
             * 整个请求到结束，搞不明白，如果只是为了这个结果，干嘛不直接查询arango，然后形成该结构，
             * 何必还要中间那些没用的过程,反正无效代码写的太多了，看的我难受的一批
             * {
             *     "properties":{
             *         "_id":"",
             *         "name":"",
             *         ...具体见person或者company的结构
             *     },
             *     "relations":null,
             *     "children":[
             *          {
             *              "properties":{
             *                  "name":"转出至个人"
             *              },
             *              "relations":null,
             *              "children":[
             *                  {
             *                      "properties":{
             *                          "_id":"",
             *                          "name":"",
             *                          ...具体见person或者company的结构
             *                      },
             *                      "relations":[
             *                          {
             *                              "_from":"",
             *                              "_to":"",
             *                              ....具体见money_flow的结构
             *                          }
             *                      ]
             *                  }
             *              ]
             *          },
             *          {
             *              "properties":{
             *                  "name":"转出至企业"
             *              }
             *              "relations":null,
             *              "children":[
             *                  ....结构同转出至个人
             *              ]
             *          },
             *          {
             *              "properties":{
             *                  "name":"个人转入"
             *              },
             *              "relations":null,
             *              "children":[
             *                  ....结构同转出至个人
             *              ]
             *          }
             *          ,
             *          {
             *              "properties":{
             *                  "name":"企业转入"
             *              },
             *              "relations":null,
             *              "children":[
             *                  ....结构同转出至个人
             *              ]
             *          }
             *     ]
             * }
             *
             */
        }
        return root;
    }
}
