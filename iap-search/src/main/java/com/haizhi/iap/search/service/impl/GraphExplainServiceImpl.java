package com.haizhi.iap.search.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.haizhi.iap.common.exception.ServiceAccessException;
import com.haizhi.iap.search.controller.GraphFoxxWS;
import com.haizhi.iap.search.controller.model.EdgesOption;
import com.haizhi.iap.search.controller.model.Graph;
import com.haizhi.iap.search.controller.model.GraphReq;
import com.haizhi.iap.search.exception.SearchException;
import com.haizhi.iap.search.repo.EnterpriseRepo;
import com.haizhi.iap.search.repo.GraphRepo;
import com.haizhi.iap.search.service.GraphExplainService;
import com.haizhi.iap.search.service.GraphService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.common.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by chenbo on 2017/8/9.
 */
@Slf4j
@Service
public class GraphExplainServiceImpl implements GraphExplainService {

    @Setter
    @Autowired
    GraphFoxxWS graphFoxxWS;

    @Setter
    @Autowired
    GraphService graphService;

    @Setter
    @Autowired
    GraphRepo graphRepo;

    @Setter
    @Autowired
    GraphReq graphOriginalReq;

    @Setter
    @Autowired
    EnterpriseRepo enterpriseRepo;

    private Graph explainActualControlRule1(List<String> fromList, String to) {
        GraphReq req;

        Map<String, Object> vertexMap = Maps.newHashMap();
        Map<String, Object> edgeMap = Maps.newHashMap();
        List<Map<String, Object>> edges = Lists.newArrayList();

        Map<String, Object> toEntity = graphRepo.fetchDocument(to);
        if (fromList != null && to != null && toEntity != null) {
            req = processCompanyReq(toEntity, "invest", "person_merge", "shareholder", "tradable_share", "officer");
            for (String from : fromList) {
                Map<String, Object> fromEntity = graphRepo.fetchDocument(from);
                vertexMap.put(from, fromEntity);
                Graph graph = graphService.buildGraph(to, req, false);
                edges.addAll(extractRelationFromGraph(fromEntity, graph));
                edges.addAll(graphRepo.getRelation("actual_controller", from, to, false));
            }
            vertexMap.put(to, graphRepo.fetchDocument(to));
        }
        for (Map<String, Object> edge : edges) {
            if (edge != null && edge.get("_id") != null) {
                edgeMap.put(edge.get("_id").toString(),
                        edge.get("label") == null || Strings.isNullOrEmpty(edge.get("label").toString()) ?
                                graphService.addLabel(edge) : edge);
            }
        }
        return Graph.genVirtualGraph(vertexMap, edgeMap, graphRepo);
    }

    private Graph explainActualControlRule2(List<String> fromList, List<String> depthList, String to) {
        Graph graph = null;
        GraphReq req;

        Map<String, Object> toEntity = graphRepo.fetchDocument(to);
        if (fromList != null && to != null && toEntity != null) {
            req = processReq("invest", "person_merge");
            req.setFromList(fromList);
            req.setDepthList(depthList);
            req.setTo(to);

            Map<String, Object> resp = graphRepo.findPathByIds(req);
            List<Map<String, Object>> edges = Lists.newArrayList();
            List<Map<String, Object>> vertexes;
            Map<String, Object> vertexMap = Maps.newHashMap();
            Map<String, Object> edgeMap = Maps.newHashMap();
            if (resp != null) {
                for (String from : fromList) {
                    edges.addAll(graphRepo.getRelation("actual_controller", from, to, false));
                }
                if (resp.get("edges") != null && resp.get("edges") instanceof List) {
                    edges.addAll((List<Map<String, Object>>) resp.get("edges"));
                    for (Map<String, Object> link : edges) {
                        edgeMap.put((String) link.get("_id"), graphService.addLabel(link));
                    }
                }

                if (resp.get("vertexes") != null) {
                    vertexes = (List<Map<String, Object>>) resp.get("vertexes");
                    for (Map<String, Object> node : vertexes) {
                        vertexMap.put((String) node.get("_id"), node);
                    }
                }


                graph = Graph.genVirtualGraph(vertexMap, edgeMap, graphRepo);
            } else {
                log.warn("graph foxx traversal return null for from {} to {} and req {}", fromList, to, req);
            }
        }

        return graph;
    }

    private Graph explainActualControlRule3(List<String> fromList, List<String> depthList, String to) {
        Graph graph = null;
        GraphReq req;

        Map<String, Object> toEntity = graphRepo.fetchDocument(to);
        if (fromList != null && to != null && toEntity != null) {
            req = processReq("invest", "tradable_share", "person_merge");
            req.setFromList(fromList);
            req.setDepthList(depthList);
            req.setTo(to);

            Map<String, Object> resp = graphRepo.findPathByIds(req);
            List<Map<String, Object>> edges = Lists.newArrayList();
            List<Map<String, Object>> vertexes = Lists.newArrayList();
            Map<String, Object> vertexMap = Maps.newHashMap();
            Map<String, Object> edgeMap = Maps.newHashMap();
            if (resp != null) {
                if (resp.get("edges") != null && resp.get("edges") instanceof List) {
                    edges = (List<Map<String, Object>>) resp.get("edges");
                }

                if (resp.get("vertexes") != null) {
                    vertexes = (List<Map<String, Object>>) resp.get("vertexes");
                }

                //遍历from节点,逐个展开 亲属关系
                Map<String, Map<String, Object>> fromEntityList = Maps.newHashMap();
                for (String from : fromList) {
                    fromEntityList.put(from, graphRepo.fetchDocument(from));
                }

                for (String from : fromList) {
                    req = processReq("family", "person_merge");
                    Graph child = graphService.buildGraph(from, req, false);

                    for (String inner : fromList) {
                        if (!inner.equals(from)) {
                            edges.addAll(extractRelationFromGraph(fromEntityList.get(inner), child));
                            //亲属关系展示需要将"互为亲属"这个实体保留
                            vertexes.addAll(extractVertexFromGraph("Family", child));
                        }
                    }
                    edges.addAll(graphRepo.getRelation("actual_controller", from, to, false));
                }

                for (Map<String, Object> link : edges) {
                    edgeMap.put((String) link.get("_id"), graphService.addLabel(link));
                }
                for (Map<String, Object> node : vertexes) {
                    vertexMap.put((String) node.get("_id"), node);
                }
                graph = Graph.genVirtualGraph(vertexMap, edgeMap, graphRepo);
            } else {
                log.warn("graph foxx traversal return null for from {} to {} and req {}", fromList, to, req);
            }
        }

        return graph;
    }

    private Graph explainActualControlRule4(List<String> fromList, List<String> depthList, String to) {
        Graph graph = null;
        GraphReq req;

        Map<String, Object> toEntity = graphRepo.fetchDocument(to);
        if (fromList != null && to != null && toEntity != null) {
            req = processReq("invest", "person_merge");
            req.setFromList(fromList);
            req.setDepthList(depthList);
            req.setTo(to);

            Map<String, Object> resp = graphRepo.findPathByIds(req);
            List<Map<String, Object>> edges = Lists.newArrayList();
            List<Map<String, Object>> vertexes = Lists.newArrayList();
            Map<String, Object> vertexMap = Maps.newHashMap();
            Map<String, Object> edgeMap = Maps.newHashMap();
            if (resp != null) {
                if (resp.get("edges") != null && resp.get("edges") instanceof List) {
                    edges = (List<Map<String, Object>>) resp.get("edges");
                }

                if (resp.get("vertexes") != null) {
                    vertexes = (List<Map<String, Object>>) resp.get("vertexes");
                }

                Map<String, Map<String, Object>> fromEntityList = Maps.newHashMap();
                for (String from : fromList) {
                    fromEntityList.put(from, graphRepo.fetchDocument(from));
                    //显示实际控制关系
                    edges.addAll(graphRepo.getRelation("actual_controller", from, to, false));
                }

                //遍历from节点,逐个展开 一致行动关系
                for (String from : fromList) {
                    req = processReq("concert");
                    Graph child = graphService.buildGraph(from, req, false);

                    for (String inner : fromList) {
                        if (!inner.equals(from)) {
                            edges.addAll(extractRelationFromGraph(fromEntityList.get(inner), child));
                            //亲属关系展示需要将"互为亲属"这个实体保留
                            vertexes.addAll(extractVertexFromGraph("Family", child));
                        }
                    }
                }

                for (Map<String, Object> edge : edges) {
                    edgeMap.put((String) edge.get("_id"), edge.get("label") == null || Strings.isNullOrEmpty(edge.get("label").toString()) ?
                            graphService.addLabel(edge) : edge);
                }
                for (Map<String, Object> node : vertexes) {
                    vertexMap.put((String) node.get("_id"), node);
                }
                graph = Graph.genVirtualGraph(vertexMap, edgeMap, graphRepo);
            } else {
                log.warn("graph foxx traversal return null for from {} to {} and req {}", fromList, to, req);
            }
        }

        return graph;
    }

    @Override
    public Graph explainActualControl(String rule, List<String> fromList, List<String> depthList, String to) {
        if (rule.equalsIgnoreCase("Rule1")) {
            //个体企业以法人作为实际控制人
            return explainActualControlRule1(fromList, to);
        } else if (rule.equalsIgnoreCase("Rule2")) {
            //有限公司或自然人持股超过一半，不使用亲属关系以及一致行动关系合并
            return explainActualControlRule2(fromList, depthList, to);
        } else if (rule.equalsIgnoreCase("Rule3")) {
            //有限公司或自然人持股超过一半，使用亲属关系合并
            return explainActualControlRule3(fromList, depthList, to);
        } else if (rule.equalsIgnoreCase("Rule4")) {
            //有限公司或自然人持股超过一半，使用一致行动关系合并
            return explainActualControlRule4(fromList, depthList, to);
        } else {
            throw new ServiceAccessException(SearchException.UNIDENTIFIED_RULE);
        }
    }

    @Override
    public Graph explainConcert(String from, String to, String target, String rule) {
        Map<String, Object> vertexMap = Maps.newHashMap();
        Map<String, Object> edgeMap = Maps.newHashMap();
        List<Map<String, Object>> edges = Lists.newArrayList();
        Map<String, Object> fromEntity = graphRepo.fetchDocument(from);
        Map<String, Object> toEntity = graphRepo.fetchDocument(to);
        Map<String, Object> targetEntity = graphRepo.fetchDocument(target);
        Graph graph = null;
        Map<String, Object> pathResult = null;

        String person = null;
        String company = null;
        Map<String, Object> personEntity = null;
        Map<String, Object> companyEntity = null;
        if (from.startsWith("Person") && to.startsWith("Company")) {
            person = from;
            personEntity = fromEntity;
            company = to;
            companyEntity = toEntity;
        } else if (to.startsWith("Person") && from.startsWith("Company")) {
            person = to;
            personEntity = toEntity;
            company = from;
            companyEntity = fromEntity;
        }

        GraphReq req;
        switch (rule) {
            case "Rule1":
                vertexMap.put(from, fromEntity);
                vertexMap.put(to, toEntity);
                vertexMap.put(target, targetEntity);

                req = processCompanyReq(targetEntity, "control_shareholder");
                graph = graphService.buildGraph(to, req, false);
                edges.addAll(extractRelationFromGraph(fromEntity, graph));
                graph = graphService.buildGraph(from, req, false);
                edges.addAll(extractRelationFromGraph(toEntity, graph));

                //与target相关路径去图里找
                if (target != null && target.startsWith("Company/")) {
                    req = processCompanyReq(targetEntity, "tradable_share", "shareholder", "invest");
                    graph = graphService.buildGraph(target, req, false);
                    edges.addAll(extractRelationFromGraph(toEntity, graph));
                    edges.addAll(extractRelationFromGraph(fromEntity, graph));
                }

                break;
            case "Rule2":
                vertexMap.put(from, fromEntity);
                vertexMap.put(to, toEntity);
                vertexMap.put(target, targetEntity);

                if (target != null && target.startsWith("Company/")) {
                    req = processCompanyReq(targetEntity, "tradable_share", "shareholder");
                    graph = graphService.buildGraph(target, req, false);
                    edges.addAll(extractRelationFromGraph(toEntity, graph));
                    edges.addAll(extractRelationFromGraph(fromEntity, graph));
                }
                List<Map<String, Object>> commonControl1 = graphRepo.getRelation("control_shareholder", null, from, true);
                List<Map<String, Object>> commonControl2 = graphRepo.getRelation("control_shareholder", null, to, true);
                //找出common vertext
                for (Map<String, Object> controlEdge1 : commonControl1) {
                    for (Map<String, Object> controlEdge2 : commonControl2) {
                        if (controlEdge1.get("_from").equals(controlEdge2.get("_from"))) {
                            vertexMap.put(controlEdge1.get("_from").toString(),
                                    graphRepo.fetchDocument(controlEdge1.get("_from").toString()));
                            break;
                        }
                    }
                }
                edges.addAll(commonControl1);
                edges.addAll(commonControl2);
                edges.addAll(graphRepo.getRelation("control_shareholder", null, to, true));

                break;
            case "Rule3":
                fromEntity = graphRepo.fetchDocument(from);
                toEntity = graphRepo.fetchDocument(to);

                req = processCompanyReq(targetEntity, "officer", "person_merge");

                pathResult = graphRepo.getShortestPath(fromEntity.get("name").toString(), toEntity.get("name").toString(), req);
                break;
            case "Rule7":
                vertexMap.put(from, fromEntity);
                vertexMap.put(to, toEntity);
                vertexMap.put(target, targetEntity);

                if (target != null && target.startsWith("Company/")) {
                    req = processCompanyReq(targetEntity, "tradable_share", "shareholder", "invest", "person_merge");
                    graph = graphService.buildGraph(target, req, false);
                    edges.addAll(extractRelationFromGraph(toEntity, graph));
                    edges.addAll(extractRelationFromGraph(fromEntity, graph));
                }
                edges.addAll(graphRepo.getRelation("invest", from, to, false));

                //from和to之间的投资展示
                if (from.startsWith("Person") || to.startsWith("Person")) {
                    req = processCompanyReq(companyEntity, "tradable_share", "shareholder", "invest", "person_merge");
                    graph = graphService.buildGraph(company, req, false);
                    edges.addAll(extractRelationFromGraph(personEntity, graph));
                }

                break;
            case "Rule8":
                vertexMap.put(from, fromEntity);
                vertexMap.put(to, toEntity);
                vertexMap.put(target, targetEntity);

                if (target != null && target.startsWith("Company/")) {
                    req = processCompanyReq(targetEntity, "tradable_share", "shareholder", "invest", "person_merge");
                    graph = graphService.buildGraph(target, req, false);
                    edges.addAll(extractRelationFromGraph(toEntity, graph));
                    edges.addAll(extractRelationFromGraph(fromEntity, graph));
                }

                //from和to之间的高管
                if (from.startsWith("Person") && to.startsWith("Company")) {
                    req = processCompanyReq(toEntity, "officer");
                    graph = graphService.buildGraph(to, req, false);
                    edges.addAll(extractRelationFromGraph(fromEntity, graph));
                } else if (to.startsWith("Person") && from.startsWith("Company")) {
                    req = processCompanyReq(fromEntity, "officer");
                    graph = graphService.buildGraph(from, req, false);
                    edges.addAll(extractRelationFromGraph(toEntity, graph));
                }

                break;
            case "Rule9":
                vertexMap.put(from, fromEntity);
                vertexMap.put(to, toEntity);
                vertexMap.put(target, targetEntity);

                if (target != null && target.startsWith("Company/")) {
                    req = processCompanyReq(targetEntity, "tradable_share", "shareholder", "invest", "person_merge");
                    graph = graphService.buildGraph(target, req, false);
                    edges.addAll(extractRelationFromGraph(toEntity, graph));
                    edges.addAll(extractRelationFromGraph(fromEntity, graph));
                }

                //展示from和to(eg: from为自然人, to为公司)中from的亲属x,该亲属x对to的投资
                req = processReq("family", "person_merge", "invest");
                req.getOptions().setMaxLength(3);
                req.setFromList(Lists.newArrayList(person));
                req.setTo(company);
                if (req.getOptions().getFilter().getEdge().get("invest") != null
                        && req.getOptions().getFilter().getEdge().get("invest") instanceof Map) {
                    ((Map) req.getOptions().getFilter().getEdge().get("invest"))
                            .put("invest_ratio", Collections.singletonMap("min", 30));
                }
                pathResult = graphRepo.findPathByIds(true, req);
                break;
            case "Rule10":
                vertexMap.put(from, fromEntity);
                vertexMap.put(to, toEntity);
                vertexMap.put(target, targetEntity);

                if (target != null && target.startsWith("Company/")) {
                    req = processCompanyReq(targetEntity, "tradable_share", "shareholder", "invest", "person_merge");
                    graph = graphService.buildGraph(target, req, false);
                    edges.addAll(extractRelationFromGraph(toEntity, graph));
                    edges.addAll(extractRelationFromGraph(fromEntity, graph));
                }

                req = processReq("officer", "family", "person_merge");
                req.getOptions().setMaxLength(3);
                req.setFromList(Lists.newArrayList(person));
                req.setTo(company);
                pathResult = graphRepo.findPathByIds(true, req);

                break;
            case "Rule11":
                vertexMap.put(from, fromEntity);
                vertexMap.put(to, toEntity);
                vertexMap.put(target, targetEntity);

                req = processCompanyReq(targetEntity, "family", "person_merge");
                req.setFromList(Lists.newArrayList(from));
                req.setTo(to);
                pathResult = graphRepo.findPathByIds(true, req);

                if (target != null && target.startsWith("Company/")) {
                    req = processCompanyReq(targetEntity, "tradable_share", "shareholder", "invest", "person_merge", "officer");
                    graph = graphService.buildGraph(target, req, false);
                    edges.addAll(extractRelationFromGraph(toEntity, graph));
                    edges.addAll(extractRelationFromGraph(fromEntity, graph));
                }
                break;
            case "Rule12":
                vertexMap.put(from, fromEntity);
                vertexMap.put(to, toEntity);
                vertexMap.put(target, targetEntity);

                if (target != null && target.startsWith("Company/")) {
                    req = processCompanyReq(targetEntity, "tradable_share", "shareholder", "invest", "person_merge", "officer");
                    graph = graphService.buildGraph(target, req, false);
                    edges.addAll(extractRelationFromGraph(toEntity, graph));
                    edges.addAll(extractRelationFromGraph(fromEntity, graph));
                }

                req = processReq("family", "control_shareholder", "person_merge");
                req.getOptions().setMaxLength(3);
                req.setFromList(Lists.newArrayList(person));
                req.setTo(company);
                pathResult = graphRepo.findPathByIds(true, req);
                break;
            case "Rule13":
                vertexMap.put("from", fromEntity);
                vertexMap.put(to, toEntity);
                vertexMap.put(target, targetEntity);

                if (target != null && target.startsWith("Company/")) {
                    req = processCompanyReq(targetEntity, "tradable_share", "shareholder", "invest", "person_merge", "officer");
                    graph = graphService.buildGraph(target, req, false);
                    edges.addAll(extractRelationFromGraph(toEntity, graph));
                    edges.addAll(extractRelationFromGraph(fromEntity, graph));
                }

                if (companyEntity != null) {
                    req = processCompanyReq(companyEntity, "control_shareholder");
                    graph = graphService.buildGraph(company, req, false);
                    edges.addAll(extractRelationFromGraph(personEntity, graph));
                }
                break;
            default:
                throw new ServiceAccessException(SearchException.UNIDENTIFIED_RULE);
        }

        if (pathResult != null) {
            List<String> excludeVertexIds = Lists.newArrayList();

            if (pathResult.get("vertexes") != null) {
                List<Map<String, Object>> vertexes = (List<Map<String, Object>>) pathResult.get("vertexes");
                for (Map<String, Object> node : vertexes) {
                    if (node.get("_id").toString().startsWith("Company") &&
                            (!node.get("_id").equals(to) && !node.get("_id").equals(from))) {
                        //找路径的时候可能会带出非from to target的公司,要排除掉
                        excludeVertexIds.add(node.get("_id").toString());
                    } else if (!vertexMap.keySet().contains(node.get("_id"))) {
                        vertexMap.put(node.get("_id").toString(), node);
                    }
                }
            }

            if (pathResult.get("edges") != null && pathResult.get("edges") instanceof List) {
                for (Map<String, Object> edge : (List<Map<String, Object>>) pathResult.get("edges")) {
                    if (!excludeVertexIds.contains(edge.get("_from").toString())
                            && !excludeVertexIds.contains(edge.get("_to").toString())) {
                        edges.add(edge);
                    }
                }
            }
        }

        //一致行动关系也展示
        edges.addAll(graphRepo.getRelationOfConcert(from, to, target, false));

        for (Map<String, Object> edge : edges) {
            if (edge != null && edge.get("_id") != null) {
                edgeMap.put(edge.get("_id").toString(), edge.get("label") == null || Strings.isNullOrEmpty(edge.get("label").toString()) ?
                        graphService.addLabel(edge) : edge);
            }
        }
        return Graph.genVirtualGraph(vertexMap, edgeMap, graphRepo);
    }

    private List<Map<String, Object>> extractRelationFromGraph(Map<String, Object> fromEntity, Graph graph) {
        List<Map<String, Object>> vertexes = graph.getVertexes();
        List<Map<String, Object>> edges = graph.getEdges();
        String realFromId = null;
        List<Map<String, Object>> result = Lists.newArrayList();
        //从图的顶点中找到这两个点
        for (Map<String, Object> vertex : vertexes) {
            if (vertex.get("name") != null) {
                if (vertex.get("name").equals(fromEntity.get("name").toString())) {
                    realFromId = vertex.get("_id").toString();
                    break;
                }
            }
        }
        //从图的边中找到这两个点id对应的边
        for (Map<String, Object> edge : edges) {
            if (edge.get("_from").equals(realFromId)) {
                edge.put("_from", fromEntity.get("_id").toString());
                result.add(edge);
            } else if (edge.get("_to").equals(realFromId)) {
                edge.put("_to", fromEntity.get("_id").toString());
                result.add(edge);
            } else if (edge.get("_id").toString().startsWith("family")) {
                result.add(edge);
            }
        }
        if (result.size() > 0) {
            return result;
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    private List<Map<String, Object>> extractVertexFromGraph(String vertexType, Graph graph) {
        List<Map<String, Object>> vertexes = graph.getVertexes();

        List<Map<String, Object>> result = Lists.newArrayList();
        //从图的顶点中找到这两个点
        for (Map<String, Object> vertex : vertexes) {
            if (vertex.get("_id") != null) {
                if (vertex.get("_id").toString().startsWith(vertexType)) {
                    result.add(vertex);
                }
            }
        }
        if (result.size() > 0) {
            return result;
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    private GraphReq processCompanyReq(Map<String, Object> companyEntity, String... categories) {
        GraphReq req = (GraphReq) graphOriginalReq.clone();
        if (req != null && req.getOptions() != null && req.getOptions().getEdges() != null) {
            List<String> list = Lists.newArrayList(categories);
            for (EdgesOption option : req.getOptions().getEdges()) {
                if (list.contains(option.getCategory())) {
                    option.setVisible(true);
                } else {
                    option.setVisible(false);
                }
            }
            req.getOptions().setMaxLength(10);
        }
        if (companyEntity != null && companyEntity.get("name") != null) {
            req.setCompany(companyEntity.get("name").toString());
        }
        return req;
    }

    private GraphReq processReq(String... categories) {
        GraphReq req = (GraphReq) graphOriginalReq.clone();
        if (req != null && req.getOptions() != null && req.getOptions().getEdges() != null) {
            List<String> list = Lists.newArrayList(categories);
            for (EdgesOption option : req.getOptions().getEdges()) {
                if (list.contains(option.getCategory())) {
                    option.setVisible(true);
                } else {
                    option.setVisible(false);
                }
            }
            req.getOptions().setMaxLength(5);
        }
        return req;
    }
}
