package com.haizhi.iap.search.service.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.haizhi.iap.common.exception.ServiceAccessException;
import com.haizhi.iap.common.utils.MapBuilder;
import com.haizhi.iap.common.utils.SecretUtil;
import com.haizhi.iap.search.conf.GraphLabelConf;
import com.haizhi.iap.search.controller.model.EdgesOption;
import com.haizhi.iap.search.controller.model.Graph;
import com.haizhi.iap.search.controller.model.GraphListReq;
import com.haizhi.iap.search.controller.model.GraphOptions;
import com.haizhi.iap.search.controller.model.GraphReq;
import com.haizhi.iap.search.controller.model.GraphVo;
import com.haizhi.iap.search.controller.model.PageArangoParam;
import com.haizhi.iap.search.controller.model.PageResult;
import com.haizhi.iap.search.controller.model.ReqGuaranteeOrTransfer;
import com.haizhi.iap.search.controller.model.Tree;
import com.haizhi.iap.search.enums.EdgeOptionCategory;
import com.haizhi.iap.search.enums.GraphField;
import com.haizhi.iap.search.exception.SearchException;
import com.haizhi.iap.search.model.CompanyGroup;
import com.haizhi.iap.search.model.EdgeCollection;
import com.haizhi.iap.search.model.GraphEdge;
import com.haizhi.iap.search.model.PersonFamiliars;
import com.haizhi.iap.search.repo.CompanyGroupRepo;
import com.haizhi.iap.search.repo.DetailGraphRepo;
import com.haizhi.iap.search.repo.EnterpriseRepo;
import com.haizhi.iap.search.repo.GraphRepo;
import com.haizhi.iap.search.repo.RedisRepo;
import com.haizhi.iap.search.service.GraphClustersService;
import com.haizhi.iap.search.service.GraphService;
import com.haizhi.iap.search.utils.EventDataTransformer;
import com.haizhi.iap.search.utils.InvestGroupDataTransformer;
import com.haizhi.iap.search.utils.MoneyFlowDataTransformer;
import com.haizhi.iap.search.utils.NumberUtil;
import com.haizhi.iap.search.utils.OfficerDataTransformer;
import com.haizhi.iap.search.utils.PersonMergeUtil;
import com.haizhi.iap.search.utils.StockRightDataTransformer;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by chenbo on 17/2/23.
 */
@Slf4j
@Service
@SuppressWarnings("unchecked")
public class GraphServiceImpl implements GraphService {

    @Setter
    @Autowired
    GraphRepo graphRepo;

    @Setter
    @Autowired
    GraphReq graphReq;

    @Setter
    @Autowired
    DetailGraphRepo detailGraphRepo;

    @Setter
    @Autowired
    EnterpriseRepo enterpriseRepo;

    @Setter
    @Autowired
    RedisRepo redisRepo;

    @Setter
    @Autowired
    CompanyGroupRepo companyGroupRepo;

    @Autowired
    private PersonMergeUtil personMergeUtil;
    @Autowired
    private OfficerDataTransformer officerDataTransformer;
    @Autowired
    private StockRightDataTransformer stockRightDataTransformer;
    @Autowired
    private MoneyFlowDataTransformer moneyFlowDataTransformer;
    @Autowired
    private InvestGroupDataTransformer investGroupDataTransformer;
    @Autowired
    private EventDataTransformer eventDataTransformer;

    @Setter
    @Autowired
    GraphReq req;

    @Setter
    @Autowired
    GraphClustersService graphClustersService;


    /**
     * @Deprecated Replaced by {@link com.haizhi.iap.search.repo.EnterpriseRepo#getBasic(String)} )
     */
    @Deprecated
    @Override
    public Map<String, Object> brief(String company) {
        return graphRepo.getCompanyByName(company);
    }

    @Override
    public List<String> getGroupList(String type, Integer offset, Integer count) {
        List<CompanyGroup> companyGroups = companyGroupRepo.findGroupNameByType(type, offset, count);
        List<String> groupNames = Lists.newArrayList();
        companyGroups.stream().forEach(companyGroup -> {
            groupNames.add(String.format("%s(%d)", companyGroup.getGroupName(), companyGroup.getEntityCount()));
        });
        return groupNames;
    }

    /**
    * @description 族谱列表
    * @param req
    * @return java.util.Map<String,Object>
    * @author yuding
    * @date 2018/8/22
    */
    @Override
    public Map<String, Object> getGroupList(GraphListReq req) {
        Map<String, Object> data = Maps.newHashMap();
        List<Map<String, Object>> groupList;
        Integer total;
        //当公司名不为空
        if (req.getEntityName() != null) {
            groupList = companyGroupRepo.getGroupListByEntityAndType(req);
            total = companyGroupRepo.countByTypeAndEntity(req);
        } else {
            groupList = companyGroupRepo.getGroupListByType(req);
            total = companyGroupRepo.countTotalGroup(req.getType());
        }
        data.put("data", groupList);
        data.put("total", total);
        return data;
    }

    /**
    * @description 通过ID 获取公司的详细信息
    * @param groupId 公司ID
    * @return java.util.Map<String,Object>
    * @author yuding
    * @date 2018/8/22
    */
    @Override
    public Map<String, Object> getGroupDetail(Long groupId) {
        Map<String, Object> data = Maps.newHashMap();
        if (groupId != null) {
            data = companyGroupRepo.getGroupDetail(groupId);
        }
        return data;
    }
    /**
    * @description 通过公司名称查关系概览并做了缓存（包括高管、担保、股东等六个分支）
    * @param companyName
    * @return com.haizhi.iap.search.controller.model.Tree
    * @author yuding
    * @date 2018/8/22
    */
    @Override
    public Tree queryOverviewRelation(String companyName) {
        Map<String, Object> company = brief(companyName);
        if (company == null) {
            throw new ServiceAccessException(SearchException.NO_SUCH_COMPANY);
        }
        //先去redis查询
        Tree overviewTree = redisRepo.queryOverviewRelation(companyName);
        if (overviewTree != null) {
            return overviewTree;
        }

        Tree finalTree = new Tree();
        //高管
        Tree officer = officer(companyName);
        //担保
        Tree guarantee = guarantee(companyName);
        //股东
        Tree stockRight = stockRight(companyName);
        //资金往来
        Tree moneyFlow = moneyFlow(companyName);
        //对外投资
        Tree investGroup = investGroup(companyName);
        //涉诉、招投标
        List<Tree> event = event(companyName);
        //合并
        List<Tree> final_list = new ArrayList<>();
        final_list.add(officer);
        final_list.add(stockRight);
        final_list.add(moneyFlow);
        final_list.add(investGroup);
        final_list.add(guarantee);
        final_list.addAll(event);
        //加上头节点的信息和关系和孩子
        finalTree.setChildren(final_list);
        //GraphVo officerGraph = this.officer(companyName, GraphEdge.Direction.IN, 1);
        //Tree officerTree = officerDataTransformer.graph2Tree(officerGraph);
        finalTree.setProperties(company);
        //finalTree.setRelations(officerTree.getRelations());
        //查询完存储到redis里
        redisRepo.pushOverviewRelation(finalTree,companyName);
        return finalTree;
    }

    /**
    * @description 获取关系概览的高管分支
    * @param name 公司名称
    * @return com.haizhi.iap.search.controller.model.Tree
    * @author yuding
    * @date 2018/8/22
    */
    private Tree officer(String name) {
        GraphVo officerGraph = this.officer(name, GraphEdge.Direction.IN, 1);
        Tree officerTree = officerDataTransformer.graph2Tree(officerGraph);
        Tree officerTreeNew = new Tree();
        Map<String, Object> map = new HashMap<>();
        map.put("name", "高管");
        List<Tree> children = traverse(officerTree);
        officerTreeNew.setChildren(children);
        officerTreeNew.setProperties(map);
        return officerTreeNew;
    }

    /**
    * @description 获取关系概览的股东分支
    * @param name 公司名称
    * @return com.haizhi.iap.search.controller.model.Tree
    * @author yuding
    * @date 2018/8/22
    */
    private Tree stockRight(String name) {
        GraphVo graph = this.stockRight(name, GraphEdge.Direction.IN, 2);
        Tree stockRightTree = stockRightDataTransformer.graph2Tree(graph);
        Tree stockRightTreeNew = new Tree();
        Map<String, Object> stockRightMap = new HashMap<>();
        stockRightMap.put("name", "股东");
        if (stockRightTree == null) {
            stockRightTreeNew.setChildren(null);
        } else if (stockRightTree != null) {
            for (int i = 0; i < stockRightTree.getChildren().size(); i++) {
                Tree temp = (Tree) stockRightTree.getChildren().get(i);
                if (temp != null)
                    temp.setChildren(null);
            }
            stockRightTreeNew.setChildren(stockRightTree.getChildren());
        }

        stockRightTreeNew.setProperties(stockRightMap);
        return stockRightTreeNew;
    }

    /**
    * @description 获取关系概览的资金往来分支
    * @param name 公司名称
    * @return com.haizhi.iap.search.controller.model.Tree
    * @author yuding
    * @date 2018/8/22
    */
    private Tree moneyFlow(String name) {
        GraphVo graphVo = this.moneyFlow(name, GraphEdge.Direction.ALL, 1);
        Tree moneyFlowTree = moneyFlowDataTransformer.graph2Tree(graphVo);
        Tree moneyFlowTreeNew = new Tree();
        Map<String, Object> moneyFlowMap = new HashMap<>();
        moneyFlowMap.put("name", "资金往来");
        List<Tree> moneyFlowChildren = traverse(moneyFlowTree);
        moneyFlowTreeNew.setChildren(moneyFlowChildren);
        moneyFlowTreeNew.setProperties(moneyFlowMap);
        return moneyFlowTreeNew;

    }

    /**
    * @description 获取关系概览的对外投资分支
    * @param name 公司名称
    * @return com.haizhi.iap.search.controller.model.Tree
    * @author yuding
    * @date 2018/8/22
    */
    private Tree investGroup(String name) {
        GraphVo investGroup = this.investGroup(name, GraphEdge.Direction.OUT, 1);
        Tree investGroupTree = investGroupDataTransformer.graph2Tree(investGroup);
        Tree investGroupTreeNew = new Tree();
        Map<String, Object> investGroupMap = new HashMap<>();
        investGroupMap.put("name", "对外投资");
        List<Tree> investGroupChildren = traverse(investGroupTree);
        investGroupTreeNew.setChildren(investGroupChildren);
        investGroupTreeNew.setProperties(investGroupMap);
        return investGroupTreeNew;
    }

    /**
    * @description 重构公司虚拟节点下的所有节点 并肩虚拟节点的信息作为relationName字段存储到子节点的properties
    * @param tree 虚拟节点下的tree
    * @return java.util.List<com.haizhi.iap.search.controller.model.Tree>
    * @author yuding
    * @date 2018/8/22
    */
    private List<Tree> traverse(Tree tree) {
        List<Tree> Children = new ArrayList<>();
        if (tree != null) {
            if (tree.getChildren() != null) {
                for (int i = 0; i < tree.getChildren().size(); i++) {
                    Tree temp = (Tree) tree.getChildren().get(i);
                    Object relationName = temp.getProperties().get("name");
                    if (temp.getChildren() != null) {
                        for (int j = 0; j < temp.getChildren().size(); j++) {
                            Tree temp1 = (Tree) temp.getChildren().get(j);
                            temp1.getProperties().put("relationName", relationName);
                        }
                        Children.addAll(temp.getChildren());
                    }
                }
            }
        }
        return Children;
    }

    /**
    * @description 关系概览的关联担保分支
    * @param name
    * @return com.haizhi.iap.search.controller.model.Tree
    * @author yuding
    * @date 2018/8/22
    */
    public Tree guarantee(String name) {
        GraphOptions options = new GraphOptions();
        List<EdgesOption> edgesList = new ArrayList<>();
        EdgesOption edgesOptions = new EdgesOption();
        edgesOptions.setVisible(true);
        edgesOptions.setTraceDepth(1);
        edgesOptions.setCategory("guarantee");
        edgesList.add(edgesOptions);
        options.setEdges(edgesList);


        String id = "Company/" + SecretUtil.md5(name);
        GraphReq req = new GraphReq();
        req.setOptions(options);

        Graph guaranteeGraph = this.buildGraph(id, req, false);
        Map<String, Object> guaranteeMap = new HashMap<>();
        guaranteeMap.put("name", "担保");
        Tree guaranteeTreeNew = new Tree();
        List<Tree> guaranteeChildren = new ArrayList<>();
        HashMap<String, Tree> childrenDict = new HashMap<>();
        List<Map<String, Object>> edges = guaranteeGraph.getEdges();
        List<Map<String, Object>> nodes = guaranteeGraph.getVertexes();
        for (int j = 0; j < edges.size(); j++) {
            String src = (String) edges.get(j).get("_from");
            String dst = (String) edges.get(j).get("_to");
            //如果查询现有公司名等于src名
            if (id.equals(src)) {
                //出现互相担保的情况就要判断是否已经存在该子节点，若存在，直接加上关系
                if (childrenDict.containsKey(dst)) {
                    Tree<List> temp = childrenDict.get(dst);
                    List<Map> list = temp.getRelations();
                    list.add(edges.get(j));
                } else {
                    //不存在就重新生成一个节点,孩子为空
                    Tree treeTemp = new Tree();
                    treeTemp.setChildren(null);

                    //添加关系
                    List<Map> listRelation = new ArrayList<>();
                    listRelation.add(edges.get(j));
                    treeTemp.setRelations(listRelation);

                    //添加属性 找到对应的key
                    for (int k = 0; k < nodes.size(); k++) {
                        if (dst != null && dst.equals(nodes.get(k).get("_id"))) {
                            Map<String, Object> map1 = nodes.get(k);
                            treeTemp.setProperties(map1);
                            break;
                        }
                    }
                    //将节点放到字典 以供后面判断是否已经有该节点,并加到子节点的队列中
                    childrenDict.put(dst, treeTemp);
                    guaranteeChildren.add(treeTemp);
                }
            } else if (id.equals(dst)) {
                if (childrenDict.containsKey(src)) {
                    Tree<List> temp = childrenDict.get(src);
                    List list = temp.getRelations();
                    List arrList = new ArrayList(list);
                    arrList.add(edges.get(j));
                } else {
                    Tree treeTemp = new Tree();
                    treeTemp.setChildren(null);
                    List<Map> listRelation = new ArrayList<>();
                    listRelation.add(edges.get(j));
                    treeTemp.setRelations(listRelation);
                    for (int k = 0; k < nodes.size(); k++) {
                        if (src.equals(nodes.get(k).get("_id"))) {
                            Map<String, Object> map1 = nodes.get(k);
                            treeTemp.setProperties(map1);
                            break;
                        }

                    }
                    childrenDict.put(src, treeTemp);
                    guaranteeChildren.add(treeTemp);
                }

            }
        }
        guaranteeTreeNew.setProperties(guaranteeMap);
        guaranteeTreeNew.setChildren(guaranteeChildren);
        return guaranteeTreeNew;
    }

    /**
    * @description 关系概览的涉诉事件分支
    * @param name
    * @return java.util.List<com.haizhi.iap.search.controller.model.Tree>
    * @author yuding
    * @date 2018/8/22
    */
    private List<Tree> event(String name) {
        List<Tree> eventTreeNew = new ArrayList<>();
        GraphVo eventGraph = this.event(name, 1);
        Tree eventTree = eventDataTransformer.graph2Tree(eventGraph);
        Map<String, Object> sueMap = new HashMap<>();
        sueMap.put("name", "涉诉");
        Tree sueTreeNew = new Tree();
        String[] sue = {"起诉", "被起诉", "同为原告", "同为被告"};
        String[] party = {"甲方", "乙方"};
        List<Tree> sueChildren = new ArrayList<>();

        Map<String, Object> partyMap = new HashMap<>();
        partyMap.put("name", "招投标");
        Tree partyTreeNew = new Tree();
        List<Tree> partyChildren = new ArrayList<>();
        //在事件关联里不是涉诉就是招投标
        if (eventTree != null) {
            for (int i = 0; i < eventTree.getChildren().size(); i++) {
                Tree temp = (Tree) eventTree.getChildren().get(i);
                Object relationName = temp.getProperties().get("name");

                if (temp != null) {

                    Object val = temp.getProperties().get("name");
                    if (Arrays.asList(sue).contains(val) && temp.getChildren() != null) {
                        for (int j = 0; j < temp.getChildren().size(); j++) {
                            Tree temp1 = (Tree) temp.getChildren().get(j);
                            temp1.getProperties().put("relationName", relationName);
                        }
                        sueChildren.addAll(temp.getChildren());
                    } else if (Arrays.asList(party).contains(val) && temp.getChildren() != null) {
                        for (int j = 0; j < temp.getChildren().size(); j++) {
                            Tree temp1 = (Tree) temp.getChildren().get(j);
                            temp1.getProperties().put("relationName", relationName);
                        }
                        partyChildren.addAll(temp.getChildren());
                    }
                }
            }
        }
        sueTreeNew.setChildren(sueChildren);
        sueTreeNew.setProperties(sueMap);
        partyTreeNew.setChildren(partyChildren);
        partyTreeNew.setProperties(partyMap);
        eventTreeNew.add(sueTreeNew);
        eventTreeNew.add(partyTreeNew);
        return eventTreeNew;
    }

    @Override
    public Graph buildGraph(String id, GraphReq req, Boolean readCache) {
        //对接口数据做缓存, 图数据太耗时
        Graph graph = redisRepo.getGraphCache(id);
        if (readCache && graph != null) {
            return graph;
        }
        //处理上市公司的shareholder -> tradable_share
        req = processReq(req);

        Map<String, Object> resp = graphRepo.traversal(id, req);
        List<Map<String, Object>> edges;
        List<Map<String, Object>> vertexes;
        Map<String, Object> vertexMap = Maps.newHashMap();
        Map<String, Object> edgeMap = Maps.newHashMap();
        if (resp != null) {
            if (resp.get(GraphField.EDGES.getName()) != null && resp.get(GraphField.EDGES.getName()) instanceof List) {
                edges = (List<Map<String, Object>>) resp.get(GraphField.EDGES.getName());
                for (Map<String, Object> link : edges) {
                    edgeMap.put((String) link.get(GraphField._ID.getName()), addLabelForEdge(link));
                }
            }

            if (resp.get(GraphField.VERTEXES.getName()) != null) {
                vertexes = (List<Map<String, Object>>) resp.get(GraphField.VERTEXES.getName());
                for (Map<String, Object> node : vertexes) {
                    vertexMap.put((String) node.get(GraphField._ID.getName()), vertexTypeTrick(node));
                }
            }
            graph = Graph.genVirtualGraph(vertexMap, edgeMap, graphRepo);
        } else {
            log.warn("graph foxx traversal return null for companyId {} and req {}", id, req);
        }
        if (readCache && graph != null) {
            redisRepo.pushGraphCache(id, graph);
        }
        return graph;
    }

    @Override
    public Graph buildGraph(List<Map<String, List<Map<String, Object>>>> paths) {
        Graph graph = new Graph();
        if (!CollectionUtils.isEmpty(paths)) {
            graph.setFound(true);
            List<Map<String, Object>> vertices = paths.stream().flatMap(path -> path.get(GraphField.VERTICES.getName()).stream()).filter(vertex -> !CollectionUtils.isEmpty(vertex)).collect(Collectors.toList());
            List<Map<String, Object>> edges = paths.stream().flatMap(path -> path.get(GraphField.EDGES.getName()).stream()).filter(vertex -> !CollectionUtils.isEmpty(vertex)).collect(Collectors.toList());
            graph.setEdges(edges);
            graph.setVertexes(vertices);
            graph.setLength((CollectionUtils.isEmpty(vertices) ? 0 : vertices.size()) + (CollectionUtils.isEmpty(edges) ? 0 : edges.size()));
        }
        graph.setFound(false);
        return graph;
    }

    @Override
    public Map<String, Object> invest(String id, GraphReq req) {
        Map<String, Object> resp = graphRepo.traversal(id, req);
        List<Map<String, Object>> edges = Lists.newArrayList();
        List<Map<String, Object>> vertexes;
        Map<String, Map<String, Object>> vertexMap = Maps.newHashMap();

        if (resp != null) {
            if (resp.get(GraphField.EDGES.getName()) != null && resp.get(GraphField.EDGES.getName()) instanceof List) {
                edges = (List<Map<String, Object>>) resp.get(GraphField.EDGES.getName());
            }

            if (resp.get(GraphField.VERTEXES.getName()) != null) {
                vertexes = (List<Map<String, Object>>) resp.get(GraphField.VERTEXES.getName());
                for (Map<String, Object> node : vertexes) {
                    vertexMap.put((String) node.get(GraphField._ID.getName()), vertexTypeTrick(node));
                }
            }

            List<Map<String, Object>> invest = Lists.newArrayList();
            List<Map<String, Object>> invested = Lists.newArrayList();

            for (Map<String, Object> edge : edges) {
                if (edge.get(GraphField._FROM.getName()).equals(id) && edge.get(GraphField._TO.getName()) != null) {
                    invest.add(vertexMap.get(edge.get(GraphField._TO.getName())));
                }
                if (edge.get(GraphField._TO.getName()).equals(id) && vertexMap.get(edge.get(GraphField._FROM.getName())) != null) {
                    invested.add(vertexMap.get(edge.get(GraphField._FROM.getName())));
                }
            }

            Map<String, Object> result = Maps.newHashMap();
            result.put("invest", invest);
            result.put("invested", invested);
            return result;
        } else {
            log.warn("graph foxx traversal return null for companyId {} and req {}", id, req);
        }
        return null;
    }

    @Override
    public PersonFamiliars getFamiliars(String id) {
        List<Map<String, Object>> docs = graphRepo.getFamiliar(id);
        Map<String, Object> self = Maps.newHashMap();
        List<Map<String, Object>> relation = Lists.newArrayList();
        if (docs != null) {
            List<String> names = Lists.newArrayList();
            for (Map<String, Object> doc : docs) {
                Map<String, Object> item = Maps.newHashMap();
                if (doc.get(GraphField.VERTICES.getName()) instanceof List) {
                    List<Map<String, Object>> vertices = (List<Map<String, Object>>) doc.get(GraphField.VERTICES.getName());
                    for (Map<String, Object> vertice : vertices) {
                        if (vertice != null) {
                            if (vertice.get(GraphField._ID.getName()).equals(id)) {
                                self = vertice;
                            } else {
                                if (((String) vertice.get(GraphField._ID.getName())).startsWith("Person")) {
                                    item.put("person", vertice);
                                } else if (((String) vertice.get(GraphField._ID.getName())).startsWith("Company")) {
                                    item.put("company", vertice);
                                }
                            }
                        }
                    }
                    if (item.get("person") != null && item.get("person") instanceof Map) {

                        if (((Map) item.get("person")).get(GraphField.NAME.getName()) != null && !names.contains(((Map) item.get("person")).get(GraphField.NAME.getName()))) {
                            relation.add(item);
                            names.add(((Map) item.get("person")).get(GraphField.NAME.getName()).toString());
                        }

                    }
                }
            }
        }
        return new PersonFamiliars(relation, self);
    }

    @Override
    public Graph getFamiliar(String id, String target) {
        List<Map<String, Object>> docs = graphRepo.getFamiliar(id);
        Map<String, Object> vertexMap = Maps.newHashMap();
        Map<String, Object> edgeMap = Maps.newHashMap();
        if (docs != null) {
            for (Map<String, Object> doc : docs) {
                List<Map<String, Object>> vertices = Lists.newArrayList();
                List<Map<String, Object>> edges = Lists.newArrayList();
                if (doc.get(GraphField.VERTICES.getName()) instanceof List) {
                    vertices = (List<Map<String, Object>>) doc.get(GraphField.VERTICES.getName());
                }
                if (doc.get(GraphField.EDGES.getName()) != null && doc.get(GraphField.EDGES.getName()) instanceof List) {
                    edges = (List<Map<String, Object>>) doc.get(GraphField.EDGES.getName());
                }

                if (target != null) {
                    boolean isRecord = false;

                    for (Map<String, Object> vertex : vertices) {
                        if (vertex != null && vertex.get(GraphField._ID.getName()).equals(target)) {
                            isRecord = true;
                        }
                    }
                    if (isRecord) {
                        for (Map<String, Object> vertex : vertices) {
                            if (vertex != null) {
                                vertexMap.put((String) vertex.get(GraphField._ID.getName()), vertexTypeTrick(vertex));
                            }
                        }

                        for (Map<String, Object> edge : edges) {
                            if (edge != null) {
                                edgeMap.put((String) edge.get(GraphField._ID.getName()), addLabelForEdge(edge));
                            }
                        }
                    }
                } else {
                    for (Map<String, Object> vertex : vertices) {
                        if (vertex != null) {
                            vertexMap.put((String) vertex.get(GraphField._ID.getName()), vertexTypeTrick(vertex));
                        }
                    }
                    for (Map<String, Object> edge : edges) {
                        if (edge != null) {
                            edgeMap.put((String) edge.get(GraphField._ID.getName()), addLabelForEdge(edge));
                        }
                    }
                }
            }
        }
        return Graph.genVirtualGraph(vertexMap, edgeMap, graphRepo);
    }

    @Override
    public Graph getRelationByName(String entityNameA, String entityNameB, GraphReq req) {
        //如果只指定了company1，就只单独显示一个顶点。
        if (entityNameB == null) {
            Map<String, Object> company = graphRepo.getCompanyByName(entityNameA);
            if (company == null) {
                throw new ServiceAccessException(SearchException.NO_SUCH_COMPANY);
            } else {
                return Graph.genVirtualGraph(new MapBuilder().put((String) company.get(GraphField._ID.getName()),
                        vertexTypeTrick(company)).build(), Maps.newHashMap(), graphRepo);
            }
        } else {
            Map<String, Object> vertexMap = Maps.newHashMap();
            Map<String, Object> edgeMap = Maps.newHashMap();
            List<Map<String, Object>> nodes = Lists.newArrayList();
            List<Map<String, Object>> links = Lists.newArrayList();

            req.getOptions().getEdges().add(new EdgesOption("suspected", 1, true));
            Map<String, Object> result = graphRepo.getShortestPath(entityNameA, entityNameB, req);
            if (result != null) {
                Boolean found = (Boolean) result.get("found");
                links = (List<Map<String, Object>>) result.get(GraphField.EDGES.getName());
                if (found) {
                    nodes = (List<Map<String, Object>>) result.get(GraphField.VERTEXES.getName());
                } else {
                    Object fromObj = result.get("from");
                    if ((null != fromObj) && (fromObj instanceof Map)){
                        Map fromMap = (Map<String, Object>)fromObj;
                        if(!CollectionUtils.isEmpty(fromMap)){
                            nodes.add(fromMap);
                        }

                    }
                    Object toObj = result.get("to");
                    if((null != toObj) && (toObj instanceof Map)){
                        Map toMap = (Map<String, Object>)toObj;
                        if(!CollectionUtils.isEmpty(toMap)){
                            nodes.add(toMap);
                        }
                    }

                }
                if (nodes != null) {
                    for (Map<String, Object> node : nodes) {
                        vertexMap.put((String) node.get(GraphField._ID.getName()), vertexTypeTrick(node));
                    }
                }
                if (links != null) {
                    for (Map<String, Object> edge : links) {
                        edgeMap.put((String) edge.get(GraphField._ID.getName()), addLabelForEdge(edge));
                    }
                }

                return Graph.genVirtualGraph(vertexMap, edgeMap, found, (Integer) result.get("length"), graphRepo);
            } else {
                log.warn("graph foxx shortest path return null for company1 {}, company2 {} and req {}", entityNameA, entityNameB, req);
                return null;
            }
        }
    }

    @Override
    public Graph checkGuaranteeCircle(String companyA, String companyB) {
        if (companyB == null) {
            Map<String, Object> company = graphRepo.getCompanyByName(companyA);
            if (company == null) {
                throw new ServiceAccessException(SearchException.NO_SUCH_COMPANY);
            } else {
                return Graph.genVirtualGraph(new MapBuilder().put((String) company.get(GraphField._ID.getName()),
                        vertexTypeTrick(company)).build(), Maps.newHashMap(), graphRepo);
            }
        } else {
            Map<String, Object> vertexMap = Maps.newHashMap();
            Map<String, Object> edgeMap = Maps.newHashMap();
            List<Map<String, Object>> nodes = Lists.newArrayList();
            List<Map<String, Object>> links = Lists.newArrayList();

            Map<String, Object> result = graphRepo.checkGuaranteeCircle(companyA, companyB);
            if (result != null) {
                Boolean found = (Boolean) result.get("found");
                if (found) {
                    nodes = (List<Map<String, Object>>) result.get(GraphField.VERTEXES.getName());
                    links = (List<Map<String, Object>>) result.get(GraphField.EDGES.getName());
                } else {
                    nodes.add((Map<String, Object>) result.get("from"));
                    nodes.add((Map<String, Object>) result.get("to"));
                }

                List<Map<String, Object>> nodes_fcp = Lists.newArrayList();
                List<Map<String, Object>> links_fcp = Lists.newArrayList();

                Map<String, Object> fcp = graphRepo.findCommonParent(companyA, companyB);
                Boolean found_fcp = (Boolean) fcp.get("found");
                if (found_fcp) {
                    nodes_fcp = (List<Map<String, Object>>) result.get(GraphField.VERTEXES.getName());
                    links_fcp = (List<Map<String, Object>>) result.get(GraphField.EDGES.getName());
                }

                found = found || found_fcp;
                nodes.addAll(nodes_fcp);
                links.addAll(links_fcp);
                for (Map<String, Object> node : nodes) {
                    vertexMap.put((String) node.get(GraphField._ID.getName()), vertexTypeTrick(node));
                }
                for (Map<String, Object> link : links) {
                    edgeMap.put((String) link.get(GraphField._ID.getName()), addLabelForEdge(link));
                }
                edgeMap.put("guarantee/__virtual__", new MapBuilder()
                        .put(GraphField._ID.getName(), "guarantee/__virtual__")
                        .put(GraphField._KEY.getName(), "__virtual__")
                        .put(GraphField.LABEL.getName(), "担保（假设）")
                        .put(GraphField._FROM.getName(), result.get("from") == null ? null : ((Map) result.get("from")).get(GraphField._ID.getName()))
                        .put(GraphField._TO.getName(), result.get("to") == null ? null : ((Map) result.get("to")).get(GraphField._ID.getName()))
                        .build());
                return Graph.genVirtualGraph(vertexMap, edgeMap, found, (Integer) result.get("length"), graphRepo);
            } else {
                log.warn("graph foxx checkGuaranteeCircle return null for company1 {}, company2 {}", companyA, companyB);
                return null;
            }
        }
    }

    @Override
    public Graph groupByCompany(String companyName) {
        Map<String, Object> company = graphRepo.getCompanyByName(companyName);
        if (company == null || company.get("group_id") == null) {
            log.warn("no arangodb data for company {}", companyName);
            return null;
        }
        return groupById(company.get("group_id").toString());
    }

    @Override
    public Graph groupById(String groupId) {
        Map<String, Object> group = graphRepo.getGroup(groupId);

        if (group == null) {
            log.warn("on arangodb data for getPathTypes {}", groupId);
            return null;
        }
        Map<String, Object> vertexMap = Maps.newHashMap();
        Map<String, Object> edgeMap = Maps.newHashMap();

        List<Map<String, Object>> nodes = (List<Map<String, Object>>) group.get("neighbours");
        List<Map<String, Object>> links = (List<Map<String, Object>>) group.get("edges_within_neighbours");

        if (nodes != null) {
            for (Map<String, Object> node : nodes) {
                vertexMap.put((String) node.get(GraphField._ID.getName()), vertexTypeTrick(node));
            }
        }
        if (links != null) {
            for (Map<String, Object> link : links) {
                edgeMap.put((String) link.get(GraphField._ID.getName()), addLabelForEdge(link));
            }
        }

        return Graph.genVirtualGraph(vertexMap, edgeMap, graphRepo);
    }

    @Override
    public Graph groupByEntityId(String entityId) {
        Map<String, Object> doc = graphRepo.fetchDocument(entityId);
        if (doc == null || doc.get("group_id") == null) {
            log.warn("no arango data for entity {}", entityId);
            return null;
        }
        Map<String, Object> group = graphRepo.getGroup(doc.get("group_id").toString());

        if (group == null) {
            log.warn("on arangodb data for getPathTypes {}", doc.get("group_id").toString());
            return null;
        }
        Map<String, Object> vertexMap = Maps.newHashMap();
        Map<String, Object> edgeMap = Maps.newHashMap();

        List<Map<String, Object>> nodes = (List<Map<String, Object>>) group.get("neighbours");
        List<Map<String, Object>> links = (List<Map<String, Object>>) group.get("edges_within_neighbours");

        if (nodes != null) {
            for (Map<String, Object> node : nodes) {
                vertexMap.put((String) node.get(GraphField._ID.getName()), vertexTypeTrick(node));
                // 加虚拟社群的边
                if (!doc.get(GraphField._ID.getName()).toString().equals(node.get(GraphField._ID.getName()))) {
                    String virtaulEdgeId = String.format("_virtual_%s_%s", doc.get(GraphField._ID.getName()), node.get(GraphField._ID.getName()));
                    edgeMap.put(virtaulEdgeId, new MapBuilder()
                            .put(GraphField._ID.getName(), virtaulEdgeId)
                            .put(GraphField._KEY.getName(), virtaulEdgeId)
                            .put(GraphField.LABEL.getName(), "同社群")
                            .put(GraphField._FROM.getName(), doc.get(GraphField._ID.getName()))
                            .put(GraphField._TO.getName(), node.get(GraphField._ID.getName()))
                            .build());
                }
            }
        }
        if (links != null) {
            for (Map<String, Object> link : links) {
                edgeMap.put((String) link.get(GraphField._ID.getName()), addLabelForEdge(link));
            }
        }
        return Graph.genVirtualGraph(vertexMap, edgeMap, null, null, graphRepo);
    }

    @Override
    public Graph getPersonMergeSuggested(String personA, String personB) {
        List<Map<String, Object>> result1 = graphRepo.getInvestAndOfficer(personA);
        List<Map<String, Object>> result2 = graphRepo.getInvestAndOfficer(personB);
        List<Map<String, Object>> personMerge = graphRepo.getMergeSuggested(personA);
        Map<String, Object> vertexMap = Maps.newHashMap();
        Map<String, Object> edgeMap = Maps.newHashMap();
        if (result1 != null && result2 != null && personMerge != null) {
            //合并处理
            result1.addAll(result2);
            for (Map<String, Object> result : result1) {
                List<Map<String, Object>> nodes = (List<Map<String, Object>>) result.get(GraphField.VERTICES.getName());
                if (nodes != null) {
                    nodes.add(graphRepo.fetchDocument(personB));
                    nodes.stream().filter(vertex -> vertex != null).forEach(vertex -> {
                        vertexMap.put(String.valueOf(vertex.get(GraphField._ID.getName())), vertexTypeTrick(vertex));
                    });
                }
                List<Map<String, Object>> links = (List<Map<String, Object>>) result.get(GraphField.EDGES.getName());
                if (links != null) {
                    personMerge.addAll(links);
                }
                personMerge.stream().filter(edge -> edge != null).forEach(edge -> {
                    edgeMap.put(String.valueOf(edge.get(GraphField._ID.getName())), addLabelForEdge(edge));
                });
            }

        }
        return Graph.genVirtualGraph(vertexMap, edgeMap, graphRepo);
    }

    @Override
    public List<Map<String, Object>> getPersonMergeSuggestedList(String personId) {
        return graphRepo.getMergeSuggestedList(personId);
    }

    @Override
    public Map<String, Object> investOfficerAround(String key, int depth, int personMergeDepth, int limit) {
        List<Map<String, Object>> data = null;
        if (StringUtils.contains(key, "Company")) {
            data = graphRepo.getCompanyInvestOrOfficer(key, depth, limit);
        } else if (StringUtils.contains(key, "Person")) {
            data = graphRepo.getPersonInvestOrOfficer(key, personMergeDepth, depth, limit);
        }
        if (data == null || data.size() == 0) {
            return Collections.EMPTY_MAP;
        }
        Pair<Map<String, Map<String, Object>>, Map<String, Map<String, Object>>> pair = splitPath(data);
        Map<String, Object> ret = Maps.newHashMap();
        List<Map<String, Object>> vertexes = Lists.newArrayListWithCapacity(pair.getLeft().size());
        pair.getLeft().values().forEach(e -> vertexes.add(e));
        List<Map<String, Object>> edges = Lists.newArrayListWithCapacity(pair.getRight().size());
        pair.getRight().values().forEach(e -> edges.add(e));
        ret.put(GraphField.VERTEXES.getName(), vertexes);
        ret.put(GraphField.EDGES.getName(), edges);
        return ret;
    }

    @Override
    public Map<String, Object> getCompanyOrPersonById(String id) {
        if (!Strings.isNullOrEmpty(id)) {
            if (id.startsWith("Company")) {
                return graphRepo.fetchDocument(id);
            } else if (id.startsWith("Person")) {
                return graphRepo.getPersonById(id);
            }
        }
        return null;
    }

    @Override
    public Graph generateByEdges(List<Map<String, Object>> edges) {
        List<Map<String, Object>> vertexes = Lists.newArrayList();

        edges.stream()
                .map(e -> Arrays.asList((String) e.get(GraphField._FROM.getName()), (String) e.get(GraphField._TO.getName())))
                .flatMap(l -> l.stream())
                .distinct()
                .forEach(e -> {
                    Map<String, Object> v = getCompanyOrPersonById(e);
                    if (v != null) vertexes.add(v);
                });

        Map<String, Object> vertexMap = Maps.newHashMap();
        Map<String, Object> edgeMap = Maps.newHashMap();

        vertexes.stream().filter(v -> v != null && v.get(GraphField._ID.getName()) != null).forEach(v -> {
            vertexMap.put(v.get(GraphField._ID.getName()).toString(), v);
        });

        edges.stream().filter(e -> e != null && e.get(GraphField._ID.getName()) != null).forEach(e -> {
//            Map r = graphService.addLabel(e);
//            if (r != null && r.get("invest_amount") != null) {
//                Double p = NumberUtil.tryParseDouble(r.get("invest_amount").toString());
//                Object capital = graphService.getCompanyOrPersonById((String) e.get(GraphField._TO.getName())).get("capital");
//                Double total = capital == null ? 0d : NumberUtil.tryParseDouble(capital.toString());
//                if (!p.equals(0d) && !total.equals(0d)) {
//                    BigDecimal decimal = new BigDecimal(p * 100 / total);
//                    String percent = decimal.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
//                    r.put(GraphField.LABEL.getName(), String.format("投资（%.2f%s），占比%s％", p / 10000, "万元", percent));
//                }
//            }
            edgeMap.put(e.get(GraphField._ID.getName()).toString(), addLabel(e));
        });

        return Graph.genVirtualGraph(vertexMap, edgeMap, graphRepo);
    }

    @Override
    public Graph generateByEdgesNoArango(List edges, List vertexes) {
        Graph result = new Graph();
        result.setVertexes(vertexes);
        result.setEdges(edges);
        return result;
    }

    @Override
    public Integer getTotalGroupCount(String type) {
        return companyGroupRepo.getCountByType(type);
    }


    @Override
    public Map<String, Object> addLabel(Map<String, Object> edge) {
        return addLabelForEdge(edge);
    }

    public Map<String, Object> addLabelForEdge(Map<String, Object> edge) {
        if (edge == null || edge.get(GraphField._ID.getName()) == null) {
            return null;
        }
        String edgeId = (String) edge.get(GraphField._ID.getName());
        String edgeType = edgeId.split("/")[0];
        String edgeTypeLabel = GraphLabelConf.getEdgeLabelMap().get(edgeType) == null ?
                "" : GraphLabelConf.getEdgeLabelMap().get(edgeType);
        if (edgeType.equals(EdgeOptionCategory.OFFICER.getName())) {
            String positionLabel = (String) edge.get("position");
            if (!Strings.isNullOrEmpty(positionLabel)) {
                edgeTypeLabel = String.format("%s（%s）", edgeTypeLabel, positionLabel);
            }
        } else if (edgeType.equals(EdgeOptionCategory.FAMILY.getName())) {
            edgeTypeLabel = "亲属";
        } else if (edgeType.equals(EdgeOptionCategory.CONCERT.getName())) {
            edgeTypeLabel = "一致行动关系";
        } else if (edgeType.equals(EdgeOptionCategory.PERSON_MERGE_SUGGEST.getName())) {
            edgeTypeLabel = "疑似可融合";
        } else if (edgeType.equals(EdgeOptionCategory.SUE.getName())) {
            edgeTypeLabel = String.valueOf(edge.get("type"));
        } else if (edgeType.equals(EdgeOptionCategory.TRADABLE_SHARE.getName())) {
            edgeTypeLabel = "";
            List<String> tradableShareList = Lists.newArrayList();
            String totalStakeDistribution = String.valueOf(edge.get("total_stake_distribution"));
            String increaseDecreaseShares = String.valueOf(edge.get("increase_decrease_share"));
            if (!Strings.isNullOrEmpty(totalStakeDistribution)) {
                Pattern pattern = Pattern.compile("[\\d,]+");
                Matcher matcher = pattern.matcher(increaseDecreaseShares);
                if (matcher.matches()) {
                    increaseDecreaseShares = String.format("%s %s", increaseDecreaseShares, "股");
                }
                tradableShareList.add(increaseDecreaseShares);
            }
            if (!Strings.isNullOrEmpty(totalStakeDistribution)) {
                tradableShareList.add(String.format("%s%s", "占比", totalStakeDistribution));
            }
            if (tradableShareList.size() > 0) {
                edgeTypeLabel = String.join(",", tradableShareList);
            }

        } else if (edgeType.equals(EdgeOptionCategory.INVEST.getName())
                || edgeType.equals(EdgeOptionCategory.SHAREHOLDER.getName())) {
            float investAmount = 0;
            String investAmountUnit = "";
            if (edge.get("invest_amount_unit") != null) {
                investAmountUnit = edge.get("invest_amount_unit").toString();
            }
            if (edge.get("invest_amount") != null && !edge.get("invest_amount").equals("")) {
                investAmount = NumberUtil.tryParseFloat(edge.get("invest_amount").toString());
            }
            if (investAmount > 0) {
                Map<String, Object> company = graphRepo.fetchDocument(String.valueOf(edge.get(GraphField._TO.getName())));
                String percent = "";
                if (company != null && company.get("capital") != null && !"".equals(company.get("capital"))) {
                    float registFund = 0f;
                    try {
                        registFund = Float.parseFloat(company.get("capital").toString());
                    } catch (Exception ignore) {
                    }
                    if (registFund != 0) {
                        BigDecimal decimal = new BigDecimal(investAmount * 100 / registFund);
                        percent = String.format("%.2f", decimal.setScale(2, BigDecimal.ROUND_HALF_UP));
                        percent = ",占比" + percent + "%";
                    }
                }

                edgeTypeLabel = String.format("%s（%.2f%s%s）%s", edgeTypeLabel, investAmount / 10000, "万", investAmountUnit, percent);
            }
        } else if (edgeType.equals(EdgeOptionCategory.GUARANTEE.getName())) {
            edgeTypeLabel = "担保";
        } else if (edgeType.equals(EdgeOptionCategory.MONEY_FLOW.getName())) {
            edgeTypeLabel = "转账";
        } else if (edgeType.equals(EdgeOptionCategory.UPSTREAM.getName())) {
            edgeTypeLabel = "上游";
        } else if (edgeType.equals(EdgeOptionCategory.ACTUAL_CONTROLLER.getName())) {
            edgeTypeLabel = "疑似实际控制人";
        } else if (edgeType.equals(EdgeOptionCategory.CONTROL_SHAREHOLDER.getName())) {
            edgeTypeLabel = "控股股东";
        } else if (edgeType.equals(EdgeOptionCategory.COMPANY_GROUP.getName())) {
            edgeTypeLabel = "企业派系";
        } else if (edgeType.equals(EdgeOptionCategory.INDIRECT_INVEST.getName())) {
            float ratio = 0f;
            String percent = "";
            try {
                ratio = Float.parseFloat(edge.get("actual_control_ratio").toString());
            } catch (Exception ignore) {
            }
            if (ratio != 0) {
                percent = String.format("%.2f", BigDecimal.valueOf(ratio).multiply(BigDecimal.valueOf(100)).setScale(2, BigDecimal.ROUND_HALF_UP)) + "%";
            }
            edgeTypeLabel = String.format("%s度间接投资，累计占比%s", edge.get("dept"), percent);
        }
        edge.put(GraphField.LABEL.getName(), edgeTypeLabel);
        return edge;
    }

    public Map<String, Object> vertexTypeTrick(Map<String, Object> vertex) {
        if (vertex == null || vertex.get(GraphField._ID.getName()) == null) {
            return null;
        }
        String vId = (String) vertex.get(GraphField._ID.getName());
        if (vId != null) {
            String vType = vId.split("/")[0];
            vertex.put(GraphField._TYPE.getName(), vType);
            if (vType.equals("Patent_doc")) {
                vertex.put(GraphField.NAME.getName(), vertex.get("title"));
            }
        }
        return vertex;
    }

    private Pair<Map<String, Map<String, Object>>, Map<String, Map<String, Object>>> splitPath(List<Map<String, Object>> result) {
        Map<String, Map<String, Object>> vertexMap = Maps.newHashMap();
        Map<String, Map<String, Object>> edgeMap = Maps.newHashMap();
        if (result != null) {
            for (Map<String, Object> resp : result) {
                if (resp.get(GraphField.VERTICES.getName()) != null && resp.get(GraphField.VERTICES.getName()) instanceof List) {
                    List<Map<String, Object>> nodes = (List<Map<String, Object>>) resp.get(GraphField.VERTICES.getName());
                    for (Map<String, Object> node : nodes) {
                        if(node != null){
                            vertexMap.put((String) node.get(GraphField._ID.getName()), vertexTypeTrick(node));
                        }
                    }
                }

                if (resp.get(GraphField.EDGES.getName()) != null && resp.get(GraphField.EDGES.getName()) instanceof List) {
                    List<Map<String, Object>> edges = (List<Map<String, Object>>) resp.get(GraphField.EDGES.getName());
                    for (Map<String, Object> link : edges) {
                        edgeMap.put((String) link.get(GraphField._ID.getName()), addLabelForEdge(link));
                    }
                }
            }
        }
        return Pair.of(vertexMap, edgeMap);
    }

    @Override
    public PageResult getArangoListByPage(ReqGuaranteeOrTransfer req) {
        PageArangoParam param = this.graphRepo.getPageArangoParam(req);

        return this.graphRepo.getArangoListByPageMoreCondition(param);
    }

    @Override
    public Map<String, List> getPathBySingleCollection(String collection, int depth, String id) {
        String label = "";
        if ("personal_money_flow".equals(collection)) {
            label = "转账";
        }
        return buildPathForD3(this.graphRepo.getPathBySingleCollection(collection, depth, id),
                true, label);
    }

    /**
     * isMergeEdge:是否合并边
     * 如果合并边 则 只有 _from _to isTwoWay 三个参数有效（isTwoWay 不一定存在）
     */
    private Map<String, List> buildPathForD3(List<Map<String, Object>> allPath, boolean isMergeEdge, String label) {
        Map<String, List> d3Path = new HashMap<String, List>();
        List allV = new ArrayList();
        List allE = new ArrayList();

        d3Path.put(GraphField.VERTEXES.getName(), allV);
        d3Path.put(GraphField.EDGES.getName(), allE);
        Set set = new HashSet();

        Map<String, List<Map>> midEdge = new HashMap<String, List<Map>>();

        for (Map<String, Object> item : allPath) {
            for (Object vItem : (List) item.get(GraphField.VERTICES.getName())) {
                set.add(vItem);
            }
            List midList = (List) item.get(GraphField.EDGES.getName());
            //allE.addAll(midList);

            //start ---
            if (isMergeEdge) {
                for (Map itemEdge : (List<Map>) midList) {
                    dealMergeEdge(itemEdge, midEdge, label);
                    /*
                    if(!midEdge.containsKey(itemEdge.get(GraphField._FROM.getName()))){
                        if(midEdge.containsKey(itemEdge.get(GraphField._TO.getName()))){
                            List<Map> cList = midEdge.get(itemEdge.get(GraphField._TO.getName()));
                            for(Map cItem : cList){
                                if(cItem.get(GraphField._TO.getName()).equals(itemEdge.get(GraphField._FROM.getName()))){
                                    cItem.put("isTwoWay",true);
                                    break;
                                }
                            }
                            //midEdge.get(itemEdge.get(GraphField._TO.getName())).put("isTwoWay",true);
                        }else{
                            if(!StringUtils.isEmpty(label)){
                                itemEdge.put(GraphField._LABEL.getName(),label);
                            }
                            List<Map> ll = new ArrayList<Map>();
                            ll.add(itemEdge);
                            midEdge.put(itemEdge.get(GraphField._FROM.getName()).toString(),ll);
                            //midEdge.put(itemEdge.get(GraphField._FROM.getName()).toString(),itemEdge);
                        }
                    }else{
                        List<Map> cList = midEdge.get(itemEdge.get(GraphField._FROM.getName()));
                        boolean isAdd = true;
                        for(Map cItem : cList){
                            if(cItem.get(GraphField._TO.getName()).equals(itemEdge.get(GraphField._TO.getName()))){
                                isAdd = false;
                                //cItem.put("isTwoWay",true);
                                break;
                            }
                        }
                        if(isAdd){
                            if(!StringUtils.isEmpty(label)){
                                itemEdge.put(GraphField._LABEL.getName(),label);
                            }
                            cList.add(itemEdge);
                        }
                    }
                    */
                }
            } else {
                allE.addAll(midList);
            }
            //end -----
        }
        allV.addAll(set);

        if (isMergeEdge) {
            //allE.addAll(midEdge.values());
            for (List<Map> childList : midEdge.values()) {
                allE.addAll(childList);
            }
        }

        return d3Path;
    }

    private void dealMergeEdge(Map itemEdge, Map<String, List<Map>> midEdge, String label) {
        if (midEdge.containsKey(itemEdge.get(GraphField._FROM.getName())) && midEdge.containsKey(itemEdge.get(GraphField._TO.getName()))) {
            List<Map> fromList = midEdge.get(itemEdge.get(GraphField._FROM.getName()));
            boolean isExist0 = false;
            boolean isExist1 = false;
            for (Map cEdge : fromList) {
                if (cEdge.get(GraphField._TO.getName()).equals(itemEdge.get(GraphField._TO.getName()))) {
                    isExist0 = true;
                    break;
                }
            }

            List<Map> toList = midEdge.get(itemEdge.get(GraphField._TO.getName()));
            for (Map cEdge : toList) {
                if (cEdge.get(GraphField._TO.getName()).equals(itemEdge.get(GraphField._FROM.getName()))) {
                    cEdge.put("isTwoWay", true);
                    isExist1 = true;
                    break;
                }
            }
            boolean isError = isExist0 && isExist1;
            if (isError) {
                log.error("dealMergeEdge 路径去重有重复边 ！！！");
            }
        } else if (midEdge.containsKey(itemEdge.get(GraphField._FROM.getName()))) {
            List<Map> ll = midEdge.get(itemEdge.get(GraphField._FROM.getName()));
            boolean isAdd = true;
            for (Map cEdge : ll) {
                if (cEdge.get(GraphField._TO.getName()).equals(itemEdge.get(GraphField._TO.getName()))) {
                    isAdd = false;
                    break;
                }
            }
            if (isAdd) {
                if (!StringUtils.isEmpty(label)) {
                    itemEdge.put(GraphField.LABEL.getName(), label);
                }
                ll.add(itemEdge);
            }
        } else if (midEdge.containsKey(itemEdge.get(GraphField._TO.getName()))) {
            List<Map> ll = midEdge.get(itemEdge.get(GraphField._TO.getName()));
            for (Map cEdge : ll) {
                if (cEdge.get(GraphField._TO.getName()).equals(itemEdge.get(GraphField._FROM.getName()))) {
                    cEdge.put("isTwoWay", true);
                    break;
                }
            }
        } else {
            if (!StringUtils.isEmpty(label)) {
                itemEdge.put(GraphField.LABEL.getName(), label);
            }
            List<Map> ll = new ArrayList<Map>();
            ll.add(itemEdge);
            midEdge.put(itemEdge.get(GraphField._FROM.getName()).toString(), ll);
        }

    }

    public GraphReq processReq(GraphReq req) {
        if (req != null && req.getOptions() != null) {
            Map<String, String> stockTypeMap = enterpriseRepo.getSectorMap(req.getCompany());

            /**
             * 1、shareholder的visible为true时，如果是上市公司，则将shareholder替换成tradable_share；
             *      如果不是上市公司，不做任何操作；
             * 2、shareholder的visible为false时，不做任何操作
             */
            req.getOptions().getEdges().stream().filter(option -> option.getCategory().equals("shareholder")).forEach(option -> {
                if (option.getVisible() != null) {
                    if (option.getVisible() && stockTypeMap != null && stockTypeMap.values().size() > 0) {
                        option.setCategory("tradable_share");
                        // 合并filter
                        Map<String, Object> oldFilter = (Map<String, Object>) req.getOptions().getFilter().getEdge().get("tradable_share");
                        if (oldFilter == null) {
                            oldFilter = Maps.newHashMap();
                        }
                        Map<String, Object> shareFilter = (Map<String, Object>) req.getOptions().getFilter().getEdge().get("shareholder");
                        if (shareFilter != null) {
                            for (Map.Entry<String, Object> one : shareFilter.entrySet()) {
                                oldFilter.put(one.getKey(), one.getValue());
                            }
                        }
                        req.getOptions().getFilter().getEdge().put("tradable_share", oldFilter);
                    }
                }
            });
            // 太丑了，历史原因
            req.getOptions().getEdges().stream().filter(option -> option.getCategory().equals("invest")).forEach(option -> {
                if (option.getVisible() != null && option.getVisible()) {
                    Map<String, Object> oldFilter = (Map<String, Object>) req.getOptions().getFilter().getEdge().get("tradable_share");
                    if (oldFilter == null) {
                        oldFilter = Maps.newHashMap();
                    }
                    Map<String, Object> investFilter = (Map<String, Object>) req.getOptions().getFilter().getEdge().get("invest");
                    if (investFilter != null) {
                        for (Map.Entry<String, Object> one : investFilter.entrySet()) {
                            if (one.getKey().equals("invest_ratio")) {
                                oldFilter.put("invest_ratio", one.getValue());
                            }
                        }
                    }
                    req.getOptions().getFilter().getEdge().put("tradable_share", oldFilter);
                }
            });
        }

        return req;
    }

    @Override
    public Graph getRelationById(String companyIdA, String companyIdB, GraphReq req) {
        if (companyIdB == null) {
            Map<String, Object> company = graphRepo.fetchDocument(companyIdA);
            if (company == null) {
                throw new ServiceAccessException(SearchException.NO_SUCH_COMPANY);
            } else {
                return Graph.genVirtualGraph(new MapBuilder().put((String) company.get(GraphField._ID.getName()),
                        vertexTypeTrick(company)).build(), Maps.newHashMap(), graphRepo);
            }
        }

        req.setFromList(Collections.singletonList(companyIdA));
        req.setTo(companyIdB);
        Map<String, Object> resp = graphRepo.findPathByIds(true, true, req);
        Map<String, Object> vertexMap = Maps.newHashMap();
        Map<String, Object> edgeMap = Maps.newHashMap();
        if (resp != null) {
            if (resp.get(GraphField.VERTEXES.getName()) != null && resp.get(GraphField.VERTEXES.getName()) instanceof List) {
                List<Map<String, Object>> nodes = (List<Map<String, Object>>) resp.get(GraphField.VERTEXES.getName());
                for (Map<String, Object> node : nodes) {
                    vertexMap.put((String) node.get(GraphField._ID.getName()), vertexTypeTrick(node));
                }
            }

            if (resp.get(GraphField.EDGES.getName()) != null && resp.get(GraphField.EDGES.getName()) instanceof List) {
                List<Map<String, Object>> edges = (List<Map<String, Object>>) resp.get(GraphField.EDGES.getName());
                for (Map<String, Object> link : edges) {
                    edgeMap.put((String) link.get(GraphField._ID.getName()), addLabelForEdge(link));
                }
            }
        }
        return Graph.genVirtualGraph(vertexMap, edgeMap, graphRepo);
    }

    /**
    * @description 查询指定公司的股权信息
    * @param name
    * @param direction
    * @param depth
    * @return com.haizhi.iap.search.controller.model.GraphVo
    * @author liulu
    * @date 2018/12/18
    */
    @Override
    public GraphVo stockRight(String name, GraphEdge.Direction direction, int depth) {
        Map<String, Object> company = brief(name);
        if (company == null) return null;
        String id = company.get(GraphField._ID.getName()).toString();

        List<Map<String, Map<String, Object>>> results = new ArrayList<>();
        Set<String> startVertices = new HashSet<>();
        startVertices.add(id);
        for (int depthIndex = 1; depthIndex <= depth; ++depthIndex) {
            //search tradable_share data first
            List<Map<String, Map<String, Object>>> tradableShareResults = personMergeUtil.expandWithPersonMerge(
                    StringUtils.join(EdgeCollection.TRADABLE_SHARE.getVertexCollection(), ", "),
                    Collections.singleton(EdgeCollection.TRADABLE_SHARE.getTableName()), 1, startVertices, direction, null, null
            );

            List<Map<String, Map<String, Object>>> investResults = personMergeUtil.expandWithPersonMerge(
                    StringUtils.join(EdgeCollection.INVEST.getVertexCollection(), ", "),
                    Collections.singleton(EdgeCollection.INVEST.getTableName()), 1, startVertices, direction, null, null
            );

            List<Map<String, Map<String, Object>>> _result = new ArrayList<>();
            if (!CollectionUtils.isEmpty(tradableShareResults)) _result.addAll(tradableShareResults);
            if (!CollectionUtils.isEmpty(investResults)) _result.addAll(investResults);
            if (!CollectionUtils.isEmpty(_result)) {
                startVertices.clear();
                startVertices = _result.stream().map(stockRight -> {
                           Map<String,Object> vertexInfo =  stockRight.get(GraphField.VERTEXES.getName());
                           if (!CollectionUtils.isEmpty(vertexInfo)){
                               Object idObj =  vertexInfo.get(GraphField._ID.getName());
                               if (null != idObj){
                                   return idObj.toString();
                               }
                               return null;
                           }
                           return null;
                        }
                       ).filter(Objects::nonNull).collect(Collectors.toSet());
                results.addAll(_result);
            } else break;
        }

        if (!CollectionUtils.isEmpty(results)) {
            List<Map<String, Object>> vertices = results.stream().map(map -> map.get(GraphField.VERTEXES.getName())).filter(vertex -> !CollectionUtils.isEmpty(vertex)).distinct().collect(Collectors.toList());
            vertices.add(0, company);

            List<Map<String, Object>> edges = results.stream().map(map -> map.get(GraphField.EDGES.getName())).filter(edge -> !CollectionUtils.isEmpty(edge)).distinct().collect(Collectors.toList());
            List<String> vertexIds = vertices.stream().map(vertex -> vertex.get(GraphField._ID.getName()).toString()).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(vertexIds)) {
                //get actual_controller
                List<Map<String, Map<String, Object>>> actualControllers = personMergeUtil.expandWithPersonMergeRangedResult(
                        StringUtils.join(EdgeCollection.ACTUAL_CONTROLLER.getVertexCollection(), ", "),
                        Collections.singleton(EdgeCollection.ACTUAL_CONTROLLER.getTableName()), 1, Collections.singleton(id), direction, null, Collections.singletonMap(GraphField.VERTICES.getName(), vertexIds)
                );

                //get control_shareholder
                List<Map<String, Map<String, Object>>> controlShareHolders = personMergeUtil.expandWithPersonMergeRangedResult(
                        StringUtils.join(EdgeCollection.CONTROL_SHAREHOLDER.getVertexCollection(), ", "),
                        Collections.singleton(EdgeCollection.CONTROL_SHAREHOLDER.getTableName()), 1, Collections.singleton(id), direction, null, Collections.singletonMap(GraphField.VERTICES.getName(), vertexIds)
                );

                if (!CollectionUtils.isEmpty(actualControllers)) {
                    List<Map<String, Object>> actualControllerEdges = actualControllers.stream().map(map -> map.get("edges")).filter(edge -> !CollectionUtils.isEmpty(edge)).distinct().collect(Collectors.toList());
                    edges.addAll(actualControllerEdges);
                }

                if (!CollectionUtils.isEmpty(controlShareHolders)) {
                    List<Map<String, Object>> controlShareHolderEdges = controlShareHolders.stream().map(map -> map.get("edges")).filter(edge -> !CollectionUtils.isEmpty(edge)).distinct().collect(Collectors.toList());
                    edges.addAll(controlShareHolderEdges);
                }
            }

            Map<String, Map<String, Object>> idVertexMap = new HashMap<>();
            vertices.forEach(vertex -> idVertexMap.putIfAbsent(vertex.get(GraphField._ID.getName()).toString(), vertex));

            DirectedGraph<Map<String, Object>, Map<String, Object>> directedGraph = DirectedSparseMultigraph.<Map<String, Object>, Map<String, Object>>getFactory().get();
            vertices.forEach(directedGraph::addVertex);
            edges.forEach(edge -> {
                directedGraph.addEdge(edge, idVertexMap.get(edge.get(GraphField._FROM.getName()).toString()), idVertexMap.get(edge.get(GraphField._TO.getName()).toString()));
            });

            vertices.forEach(vertex -> {
                //首选tradable_share的孩子。只要有tradable_share的孩子，就不需要有invest的孩子
                Collection<Map<String, Object>> inEdges = directedGraph.getInEdges(vertex);
                long cnt = inEdges.stream().filter(edge -> EdgeCollection.TRADABLE_SHARE.getTableName().equals(StringUtils.substringBefore(edge.get(GraphField._ID.getName()).toString(), "/"))).count();
                if (cnt > 0) {
                    //remove invest edges
                    List<Map<String, Object>> investEdges = inEdges.stream().filter(edge -> EdgeCollection.INVEST.getTableName().equals(StringUtils.substringBefore(edge.get(GraphField._ID.getName()).toString(), "/"))).collect(Collectors.toList());
                    investEdges.forEach(directedGraph::removeEdge);
                }
            });

            //remove orphan tree node
            vertices.stream().filter(vertex -> directedGraph.containsVertex(vertex) && directedGraph.degree(vertex) == 0).forEach(directedGraph::removeVertex);
            vertices = new ArrayList<>(directedGraph.getVertices());
            edges = new ArrayList<>(directedGraph.getEdges());

            //find and put the central vertex in the first place
            Map<String, Object> centralVertex = vertices.stream().filter(vertex -> id.equals(vertex.get(GraphField._ID.getName()).toString())).findAny().orElse(null);
            if (centralVertex != null) {
                vertices.remove(centralVertex);
                vertices.add(0, centralVertex);
            }
            return new GraphVo(vertices, edges);
        }
        return null;
    }

    @Override
    public GraphVo moneyFlow(String name, GraphEdge.Direction direction, int depth) {
        Map<String, Object> company = brief(name);
        if (company == null) return null;
        String id = company.get(GraphField._ID.getName()).toString();

        List<Map<String, Map<String, Object>>> results = personMergeUtil.expandWithPersonMerge(
                StringUtils.join(EdgeCollection.MONEY_FLOW.getVertexCollection(), ", "),
                Collections.singleton(EdgeCollection.MONEY_FLOW.getTableName()), depth, Collections.singleton(id), direction, null, null
        );
        return buildGraphFromTraverseResult(results, company);
    }

    @Override
    public GraphVo companyGroup(String name) {
        Map<String, Object> company = brief(name);
        if (company == null) return null;
        String id = company.get(GraphField._ID.getName()).toString();
        List<Map<String, Map<String, Object>>> results = new ArrayList<>();

        //tradable_share 出资比例大于10%，向外1层
        String filterStr = String.format("FILTER (e != null AND IS_SAME_COLLECTION('%s', e) AND TO_NUMBER(SUBSTRING(e.total_stake_distribution, 0, FIND_FIRST(e.total_stake_distribution, '%%'))) > 10)", EdgeCollection.TRADABLE_SHARE.getTableName());
        List<Map<String, Map<String, Object>>> _results = personMergeUtil.expandWithPersonMerge(
                StringUtils.join(EdgeCollection.TRADABLE_SHARE.getVertexCollection(), ", "), Collections.singleton(EdgeCollection.TRADABLE_SHARE.getTableName()), 1, Collections.singleton(id), GraphEdge.Direction.OUT, filterStr, null
        );
        if (!CollectionUtils.isEmpty(_results)) results.addAll(_results);

        //invest 出资比例 >= 20%，向外1层
        filterStr = String.format("LET capital = TO_NUMBER(DOCUMENT(e._to).capital)\n" +
                "FILTER (e != null AND IS_SAME_COLLECTION('%s', e) AND capital > 0 AND TO_NUMBER(e.invest_amount) / capital > 0.2)", EdgeCollection.INVEST.getTableName());
        _results = personMergeUtil.expandWithPersonMerge(
                StringUtils.join(EdgeCollection.INVEST.getVertexCollection(), ", "), Collections.singleton(EdgeCollection.INVEST.getTableName()), 1, Collections.singleton(id), GraphEdge.Direction.OUT, filterStr, null
        );
        if (!CollectionUtils.isEmpty(_results)) results.addAll(_results);

        //officer，ANY 2层
        _results = personMergeUtil.expandWithPersonMerge(StringUtils.join(EdgeCollection.OFFICER.getVertexCollection(), ", "), Collections.singleton(EdgeCollection.OFFICER.getTableName()), 2, Collections.singleton(id), GraphEdge.Direction.ALL, null, null);
        if (!CollectionUtils.isEmpty(_results)) results.addAll(_results);

        //control_shareholder，ANY 2层
        _results = personMergeUtil.expandWithPersonMerge(
                StringUtils.join(EdgeCollection.CONTROL_SHAREHOLDER.getVertexCollection(), ", "), Collections.singleton(EdgeCollection.CONTROL_SHAREHOLDER.getTableName()), 2, Collections.singleton(id), GraphEdge.Direction.ALL, null, null
        );
        if (!CollectionUtils.isEmpty(_results)) results.addAll(_results);

        //suspect_same_company，ANY 3层
        _results = graphRepo.traversalGraphWithParams(StringUtils.join(EdgeCollection.SUSPECT_SAME_COMPANY.getVertexCollection(), ", "),
                Collections.singleton(EdgeCollection.SUSPECT_SAME_COMPANY.getTableName()), 3, Collections.singleton(id), GraphEdge.Direction.ALL, null, null, null, null);
        if (!CollectionUtils.isEmpty(_results)) results.addAll(_results);
        return buildGraphFromTraverseResult(results, company);
    }

    @Override
    public GraphVo officer(String name, GraphEdge.Direction direction, int depth) {
        Map<String, Object> company = brief(name);
        if (company == null) return null;
        String id = company.get(GraphField._ID.getName()).toString();

        List<Map<String, Map<String, Object>>> results = personMergeUtil.expandWithPersonMerge(
                StringUtils.join(EdgeCollection.OFFICER.getVertexCollection(), ", "),
                Collections.singleton(EdgeCollection.OFFICER.getTableName()), 1, Collections.singleton(id), direction, null, null
        );
        return buildGraphFromTraverseResult(results, company);
    }

    @Override
    public GraphVo investGroup(String name, GraphEdge.Direction direction, int depth) {
        Map<String, Object> company = brief(name);
        if (company == null) return null;
        String id = company.get(GraphField._ID.getName()).toString();

        List<Map<String, Map<String, Object>>> results = Lists.newArrayList();
        //直接控股: control_shareholder.ratio > 50%
        String filterStr = "FILTER e != null AND TO_NUMBER(e.ratio) > 0.5";
        List<Map<String, Map<String, Object>>> _results = personMergeUtil.expandWithPersonMerge(
                StringUtils.join(EdgeCollection.CONTROL_SHAREHOLDER.getVertexCollection(), ", "),
                Collections.singleton(EdgeCollection.CONTROL_SHAREHOLDER.getTableName()), 1, Collections.singleton(id), direction, filterStr, null
        );
        if (!CollectionUtils.isEmpty(_results)) results.addAll(_results);

        //间接控制: actual_controller.depth > 1
        filterStr = "FILTER e != null AND TO_NUMBER(e.depth) > 1";
        _results = personMergeUtil.expandWithPersonMerge(
                StringUtils.join(EdgeCollection.ACTUAL_CONTROLLER.getVertexCollection(), ", "),
                Collections.singleton(EdgeCollection.ACTUAL_CONTROLLER.getTableName()), 1, Collections.singleton(id), direction, filterStr, null
        );
        if (!CollectionUtils.isEmpty(_results)) results.addAll(_results);

        //投资关系：全部查出，后续过滤
        _results = personMergeUtil.expandWithPersonMerge(
                StringUtils.join(EdgeCollection.INVEST.getVertexCollection(), ", "),
                Collections.singleton(EdgeCollection.INVEST.getTableName()), 1, Collections.singleton(id), direction, null, null
        );
        if (!CollectionUtils.isEmpty(_results)) results.addAll(_results);

        return buildGraphFromTraverseResult(results, company);
    }


    @Override
    public GraphVo event(String name, int depth) {
        Map<String, Object> company = brief(name);
        if (company == null) return null;
        String id = company.get(GraphField._ID.getName()).toString();

        List<EdgeCollection> edgeCollections = Arrays.asList(EdgeCollection.PARTY_BID, EdgeCollection.SUE_RELATE, EdgeCollection.PLAINTIFF_RELATE, EdgeCollection.DEFENDANT_RELATE, EdgeCollection.NEWS_ENTITY_RELATE);
        Set<String> edgeTables = edgeCollections.stream().map(EdgeCollection::getTableName).collect(Collectors.toSet());
        Set<String> vertexTables = edgeCollections.stream().flatMap(edgeCollection -> {
            String[] vertexCollections = edgeCollection.getVertexCollection();
            if (ArrayUtils.isEmpty(vertexCollections)) return null;
            return Arrays.stream(vertexCollections);
        }).filter(Objects::nonNull).collect(Collectors.toSet());

        List<Map<String, Map<String, Object>>> results = personMergeUtil.expandWithPersonMerge(StringUtils.join(vertexTables, ", "), edgeTables, 1, Collections.singleton(id), GraphEdge.Direction.ALL, null, null);
        return buildGraphFromTraverseResult(results, company);
    }

    @Override
    public Graph generateAmlGraph(@NonNull String companyName) {
        Map<String, Object> company = graphRepo.getCompanyByName(companyName);
        if (company == null) {
            throw new ServiceAccessException(SearchException.NO_SUCH_COMPANY);
        }
        GraphReq graphReq = buildAMLGraphReq(company);
        return this.buildGraph((String) company.get(GraphField._ID.getName()),graphReq,false);
    }


    private GraphReq buildAMLGraphReq(Map<String, Object> company) {
        GraphReq req = (GraphReq) graphReq.clone();
        req.setCompany((String) company.get(GraphField.NAME.getName()));

        req.getOptions().getEdges().forEach(e -> {
            if (e.getCategory().equals(EdgeCollection.SHAREHOLDER.getFilterName())
                    || e.getCategory().equals(EdgeCollection.OFFICER.getFilterName())
                    || e.getCategory().equals(EdgeCollection.TRADABLE_SHARE.getFilterName())
                    || e.getCategory().equals(EdgeCollection.INDIRECT_INVEST.getFilterName())
                    ) {
                e.setVisible(true);
            } else {
                e.setVisible(false);
            }
        });

        //构建查询条件，出资占比25%
        Map<String, Object> edge = req.getOptions().getFilter().getEdge();
        edge.put(EdgeCollection.SHAREHOLDER.getFilterName(), new MapBuilder("shareholder_ratio", new MapBuilder("min", "25").build()).put("shareholder_type", Collections.singletonList("Person")).build());
        edge.put(EdgeCollection.TRADABLE_SHARE.getFilterName(), new MapBuilder("shareholder_ratio", new MapBuilder("min", "25").build()).put("tradable_type", Collections.singletonList("Person")).build());
        edge.put(EdgeCollection.INDIRECT_INVEST.getFilterName(), new MapBuilder("actual_control_ratio", new MapBuilder("min", "25").build()).build());
        return req;
    }

}
