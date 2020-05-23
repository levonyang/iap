package com.haizhi.iap.search.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.haizhi.iap.common.utils.SecretUtil;
import com.haizhi.iap.search.controller.model.EdgesOption;
import com.haizhi.iap.search.controller.model.GraphOptions;
import com.haizhi.iap.search.controller.model.GraphReq;
import com.haizhi.iap.search.model.EdgeCollection;
import com.haizhi.iap.search.model.GraphEdge;
import com.haizhi.iap.search.model.GraphShortestPathHolder;
import com.haizhi.iap.search.model.GraphVertex;
import com.haizhi.iap.search.repo.GraphRepo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

@Slf4j
@Service
public class FoxxAdapterService {
    private static final int MAX_TRAVERSAL_DEPTH = 1;    //相当于就是一层...老代码逻辑

    private static int TRAVERSAL_BATCH_SIZE = 50;
    private static int TRAVERSAL_BATCH_COUNT = 20;
    private static int MAX_START_VERTEXES = 500;

    @Autowired
    private GraphRepo graphRepo;

    public Map<String, Object> traversal(String id, GraphReq req) {
        // collect companies
        Set<String> ids = Sets.newHashSet();
        GraphOptions options = req.getOptions();
        if (StringUtils.isNoneEmpty(id)) {
            ids.add(id.replaceAll("'", ""));
        } else {
            if (options != null && options.getIds() != null) {
                req.getOptions().getIds().stream().forEach(one -> ids.add(one.replaceAll("'", "")));
            }
        }
        // calc
        Map<String, Object> res = Maps.newHashMap();
        Set<GraphVertex> vertexes = Sets.newHashSet();
        Set<GraphEdge> edges = Sets.newHashSet();
        Map<String, Object> oneRes = traversalByDiffCollectionsAndDiffFilters(ids, options, Sets.newHashSet(), null);
        Map<String, Object> oneV = (Map<String, Object>) oneRes.get("vertexes");
        if (oneV != null) {
            for (Map.Entry<String, Object> one : oneV.entrySet()) {
                vertexes.add(new GraphVertex((Map<String, Object>) one.getValue()));
            }
        }
        Map<String, Object> oneE = (Map<String, Object>) oneRes.get("edges");
        if (oneE != null) {
            for (Map.Entry<String, Object> one : oneE.entrySet()) {
                List<Map<String, Object>> values = (List<Map<String, Object>>) one.getValue();
                for (Map<String, Object> value : values) {
                    edges.add(new GraphEdge(value));
                }
            }
        }

        List<Map<String, Object>> outV = Lists.newLinkedList();
        vertexes.stream().forEach(one -> outV.add(one.getData()));
        res.put("vertexes", outV);
        List<Map<String, Object>> outE = Lists.newLinkedList();
        edges.stream().forEach(one -> outE.add(one.getData()));
        res.put("edges", outE);
        return res;
    }

    public Map<String, Object> shortestPath(String company1, String company2, GraphReq req) {
        String c1 = "Company/" + SecretUtil.md5(company1);
        String c2 = "Company/" + SecretUtil.md5(company2);

        return doShortestPathById(c1, c2, req, true, true, false);
    }

    public Map<String, Object> checkGuaranteeCircle(String company1, String company2) {
        return null;
    }

    public Map<String, Object> findCommonParent(String companyA, String companyB) {
        return null;
    }

    public Map<String, Object> shortestPathById(String id1, String id2, GraphReq req, boolean bidirectional) {
        return doShortestPathById(id1, id2, req, bidirectional, true, false);
    }

    public Map<String, Object> shortestPathById(String id1, String id2, GraphReq req, boolean bidirectional,
                                                boolean stopIfFound) {
        return doShortestPathById(id1, id2, req, bidirectional, stopIfFound, false);
    }

    private Map<String, Object> doShortestPathById(String cid1, String cid2, GraphReq req,
                                                   boolean bidirectional, boolean stopIfFound, boolean findCommonParent) {
        GraphShortestPathHolder holder1 = new GraphShortestPathHolder(Sets.newHashSet(cid1));
        GraphShortestPathHolder holder2 = new GraphShortestPathHolder(Sets.newHashSet(cid2));

        /**
         * add by linyong 2020-04-12 写这段代码的人就是个坑逼，一行注释都不写的，鬼知道写的逻辑是什么,以为自己很牛逼的家伙，实际就是个傻屌
         */
        boolean found = false;
        boolean last_left = false;
        int minLength = -1;
        Set<GraphVertex> foundVertex = Sets.newHashSet();
        Set<GraphEdge> foundEdge = Sets.newHashSet();
        int maxLength = req.getOptions().getMaxLength();
        for (int i = 0; i < maxLength; i++) {
            log.info("from : {} -> to : {}, --------------------- step : {}", cid1, cid2, i + 1);
            GraphShortestPathHolder cur, other;
            GraphEdge.Direction direction;

            if (findCommonParent) {
                if (last_left) { //固定就是false
                    cur = holder2;
                    other = holder1;
                    direction = GraphEdge.Direction.IN;
                } else {
                    cur = holder1;
                    other = holder2;
                    direction = GraphEdge.Direction.IN;
                }
            } else {
                if (holder1.getNextStart().size() <= holder2.getNextStart().size()) {
                    cur = holder1;
                    other = holder2;
                    direction = GraphEdge.Direction.OUT;
                } else {
                    cur = holder2;
                    other = holder1;
                    direction = GraphEdge.Direction.IN;
                }
            }

            if (bidirectional) {
                direction = GraphEdge.Direction.ALL;
            }

            Map<String, Object> newRes = traversalByDiffCollectionsAndDiffFilters(cur.getNextStart(), req.getOptions(),
                    cur.getPersonMerge(), direction);
            Set<String> samePerson = Sets.newHashSet();
            mergeCurrentHolder(cur, newRes, samePerson);

            Map<String, Map<String, Object>> currentV = cur.getVertexes();
            for (Map.Entry<String, Map<String, Object>> oneV : currentV.entrySet()) {
                if (other.getVertexes().containsKey(oneV.getKey())) {
                    collectShortestPath(oneV.getKey(), cur, other, foundVertex, foundEdge);
                    found = true;
                    minLength = i + 1;
                }
            }
            if (found && stopIfFound) {
                mergeSamePersonEdges(cur, other, foundVertex, foundEdge, samePerson);
                break;
            }
        }

        Map<String, Object> res = Maps.newHashMap();
        List<Map<String, Object>> outV = Lists.newArrayList();
        foundVertex.stream().forEach(one -> outV.add(one.getData()));
        res.put("vertexes", outV);
        List<Map<String, Object>> outE = Lists.newArrayList();
        foundEdge.stream().forEach(one -> outE.add(one.getData()));
        res.put("edges", outE);
        res.put("length", minLength);
        res.put("found", found);
        res.put("from", holder1.getVertexes().get(cid1));
        res.put("to", holder2.getVertexes().get(cid2));
        return res;
    }

    private void mergeSamePersonEdges(GraphShortestPathHolder cur, GraphShortestPathHolder other, Set<GraphVertex> foundVertex, Set<GraphEdge> foundEdge, Set<String> samePerson) {
        Map<String, Map<String, Object>> curV = cur.getVertexes();
        Map<String, Map<String, Object>> otherV = other.getVertexes();
        Map<String, List<Map<String, Object>>> curEdges = cur.getPreEdges();
        Map<String, List<Map<String, Object>>> otherEdges = other.getPreEdges();
        Set<String> foundV = Sets.newHashSet();
        for (GraphVertex one : foundVertex) {
            foundV.add(one.getId());
        }
        if (samePerson.size() <= 100) {
            List<Map<String, Object>> personMerge = graphRepo.findMergedPerson(samePerson);
            for (Map<String, Object> p : personMerge) {
                Map<String, Object> node = (Map<String, Object>) p.get("vertexes");
                String newId = (String) node.get("_id");
                if ((curV.containsKey(newId) || otherV.containsKey(newId)) && !foundV.contains(newId)) {
                    // 有personMerge的边没有加进来
                    if (curV.containsKey(newId)) {
                        addMissEdgeAndVertex(curEdges, curV, newId, foundEdge, foundVertex, foundV);
                    } else if (otherV.containsKey(newId)) {
                        addMissEdgeAndVertex(otherEdges, otherV, newId, foundEdge, foundVertex, foundV);
                    }
                    foundEdge.add(new GraphEdge((Map<String, Object>) p.get("edges")));
                }
            }
        }
    }

    private void addMissEdgeAndVertex(Map<String, List<Map<String, Object>>> curEdges, Map<String, Map<String, Object>> curV, String newId, Set<GraphEdge> foundEdge, Set<GraphVertex> foundVertex, Set<String> foundV) {
        List<Map<String, Object>> missEdges = curEdges.get(newId);
        if (missEdges != null) {
            boolean found = false;
            for (Map<String, Object> edge : missEdges) {
                GraphEdge oneEdge = new GraphEdge(edge);
                String missVertex = newId.equals(oneEdge.getFrom()) ? oneEdge.getTo() : oneEdge.getFrom();
                if (foundV.contains(missVertex)) {
                    foundEdge.add(oneEdge);
                    found = true;
                }
            }
            if (found) {
                foundVertex.add(new GraphVertex(curV.get(newId)));
            }
        }
    }

    private void backtraceShortestPath(String last, GraphShortestPathHolder holder, Set<GraphVertex> foundVertex, Set<GraphEdge> foundEdge) {
        Map<String, Map<String, Object>> currentV = holder.getVertexes();
        Map<String, List<Map<String, Object>>> currentE = holder.getPreEdges();
        Queue<String> preNodes = new LinkedList<>();
        preNodes.offer(last);
        while (!preNodes.isEmpty()) {
            String v = preNodes.poll();
            if (currentV.get(v) != null && currentV.get(v).get("_id") != null) {
                foundVertex.add(new GraphVertex(currentV.get(v)));
            }
            List<Map<String, Object>> preE = currentE.get(v);
            if (preE != null) {
                for (Map<String, Object> one : preE) {
                    GraphEdge passEdge = new GraphEdge(one);
                    if (foundEdge.contains(passEdge)) {
                        continue;
                    }
                    foundEdge.add(passEdge);

                    String from = (String) one.get("_from");
                    String to = (String) one.get("_to");
                    if (v.equals(from)) {
                        if (to != null) {
                            preNodes.offer(to);
                        }
                    } else {
                        if (from != null) {
                            preNodes.offer(from);
                        }
                    }
                }
            }
        }
    }

    private void collectShortestPath(String key, GraphShortestPathHolder cur, GraphShortestPathHolder other,
                                     Set<GraphVertex> foundVertex, Set<GraphEdge> foundEdge) {
        // cur
        backtraceShortestPath(key, cur, foundVertex, foundEdge);
        // other
        backtraceShortestPath(key, other, foundVertex, foundEdge);
    }

    private void mergeCurrentHolder(GraphShortestPathHolder cur, Map<String, Object> newRes, Set<String> samePerson) {
        Map<String, Map<String, Object>> newVertexes = (Map<String, Map<String, Object>>) newRes.get("vertexes");
        Map<String, List<Map<String, Object>>> newPreEdges = (Map<String, List<Map<String, Object>>>) newRes.get("edges");

        Map<String, Map<String, Object>> currentV = cur.getVertexes();
        Set<String> next = Sets.newHashSet();
        for (Map.Entry<String, Map<String, Object>> one : newVertexes.entrySet()) {
            String id = one.getKey();
            if (id.startsWith("Person/")) {
                samePerson.add(id);
            }
            if (!currentV.containsKey(id)) {
                next.add(id);
            }
            currentV.put(id, one.getValue());
        }
        cur.setNextStart(next);

        Map<String, List<Map<String, Object>>> currentE = cur.getPreEdges();
        for (Map.Entry<String, List<Map<String, Object>>> one : newPreEdges.entrySet()) {
            if (!currentE.containsKey(one.getKey())) {
                currentE.put(one.getKey(), one.getValue());
            }
        }
    }

    private void addPreEdges(Map<String, List<Map<String, Object>>> preEdges, String nodeId, Map<String, Object> edge) {
        if (edge != null) {
            if (!preEdges.containsKey(nodeId)) {
                preEdges.put(nodeId, new LinkedList<>());
            }
            preEdges.get(nodeId).add(edge);
        }
    }

    private Map<String, Object> traversalByDiffCollectionsAndDiffFilters(final Set<String> startVertexes, GraphOptions options,
                                                                         Set<String> personMerge, GraphEdge.Direction forceDirection) {
        Map<String, Object> res = Maps.newHashMap();
        Map<String, Map<String, Object>> vertexes = Maps.newHashMap();
        Map<String, List<Map<String, Object>>> preEdges = Maps.newHashMap();
        Set<String> edgeSet = Sets.newHashSet();
        boolean hasShareholder = false;
        preDealStartVertexes(startVertexes);
        for (EdgesOption edgesOption : options.getEdges()) {
            if (edgesOption.getVisible()) {
                edgeSet.add(edgesOption.getCategory());

                // person_merge逻辑：对于每一个可融合人都往外扩
                Set<String> mergeSet = Sets.newHashSet();
                for (String vertex : startVertexes) {
                    if (vertex.indexOf("Person/") >= 0 && edgesOption.getCategory().equals(EdgeCollection.PERSON_MERGE.getTableName())) {
                        if (personMerge != null && personMerge.contains(vertex)) {
                            continue;  // 已经有了
                        }
                        mergeSet.add(vertex);
                    }
                }
                if (mergeSet.size() > 0) {
                    List<Map<String, Object>> allPerson = graphRepo.findMergedPerson(mergeSet);
                    for (Map<String, Object> p : allPerson) {
                        Map<String, Object> node = (Map<String, Object>) p.get("vertexes");
                        String newId = (String) node.get("_id");
                        startVertexes.add(newId);
                        if (personMerge != null) {
                            personMerge.add(newId);
                        }
                        // edge
                        Map<String, Object> edge = (Map<String, Object>) p.get("edges");
                        addPreEdges(preEdges, newId, edge);
                    }
                }
            } else {
                if (edgesOption.getCategory().equals("shareholder")) {
                    hasShareholder = true;
                }
            }
        }
        // 对于某一类边进行一次traversal
        for (String startVertex : startVertexes) {
            Map<String, Object> startDocument = graphRepo.fetchDocument(startVertex);
            if (startDocument != null) {
                vertexes.put(startVertex, startDocument);
            }
        }
        for (EdgesOption edgesOption : options.getEdges()) {
            if (!edgesOption.getVisible() || edgesOption.getCategory().equals(EdgeCollection.PERSON_MERGE.getTableName())) {
                continue;
            }
            String edgeName = edgesOption.getCategory().toUpperCase();
            EdgeCollection edgeCollection = EdgeCollection.valueOf(edgeName);
            if (edgeCollection == null) {
                // 支持新边遍历
                edgeCollection = new EdgeCollection(edgesOption.getCategory(), GraphEdge.Direction.ALL, EdgeCollection.DEFAULT_VERTEX_COLLECTION);
            }
            String filterStr = buildGraphFilter(edgeCollection, options);

            Integer depth = edgesOption.getTraceDepth() == null ? 1 : edgesOption.getTraceDepth();
            if (depth > MAX_TRAVERSAL_DEPTH) {
                depth = MAX_TRAVERSAL_DEPTH;
            }
            // fix direction, 上市公司方向，通过"shareholder"作判断的，太恶心了
            GraphEdge.Direction direction = edgeCollection.getDirection();
            if (edgeCollection == EdgeCollection.TRADABLE_SHARE) {
                if (edgeSet.contains("invest") && !hasShareholder) {
                    direction = GraphEdge.Direction.ALL;
                } else if (edgeSet.contains("invest") && hasShareholder) {
                    direction = GraphEdge.Direction.OUT;
                } else {
                    direction = GraphEdge.Direction.IN;
                }
            }

            List<Map<String, Object>> graphRet = null;
            if (edgeCollection.getVertexCollection().length > 0) {
                graphRet = batchTraversalGraph(edgeCollection, depth, startVertexes, forceDirection == null ? direction : forceDirection, filterStr);
            }
            if (graphRet == null) {
                continue;
            }
            for (Map<String, Object> data : graphRet) {
                Map<String, Object> node = (Map<String, Object>) data.get("vertexes");
                if (node != null) {
                    String nodeId = (String) node.get("_id");
                    if (node != null) {
                        vertexes.put(nodeId, node);
                        Map<String, Object> edge = (Map<String, Object>) data.get("edges");
                        addPreEdges(preEdges, nodeId, edge);
                    }
                }
            }
        }

        res.put("vertexes", vertexes);
        res.put("edges", preEdges);
        return res;
    }

    private void preDealStartVertexes(Set<String> startVertexes) {
        // 如果遍历的节点数过多，裁剪一下
        if (startVertexes.size() >= MAX_START_VERTEXES) {
            Set<String> cutVertexes = Sets.newHashSet();
            // 优先选择企业，后选择人
            for (String one : startVertexes) {
                if (one.startsWith("Company")) {
                    cutVertexes.add(one);
                    if (cutVertexes.size() >= MAX_START_VERTEXES) break;
                }
            }
            if (cutVertexes.size() < MAX_START_VERTEXES) {
                for (String one : startVertexes) {
                    if (one.startsWith("Person")) {
                        cutVertexes.add(one);
                        if (cutVertexes.size() >= MAX_START_VERTEXES) break;
                    }
                }
            }
            startVertexes.clear();
            startVertexes.addAll(cutVertexes);
        }
    }

    private List<Map<String, Object>> batchTraversalGraph(EdgeCollection edgeCollection, Integer depth, Set<String> startVertexes, GraphEdge.Direction direction, String filterStr) {
        List<Map<String, Object>> all = Lists.newLinkedList();
        Set<String> batch = Sets.newHashSet();
        int cnt = 0;
        for (String one : startVertexes) {
            batch.add(one);
            if (batch.size() >= TRAVERSAL_BATCH_SIZE) {
                all.addAll(graphRepo.traversalGraph(
                        StringUtils.join(edgeCollection.getVertexCollection(), ","),
                        edgeCollection.getTableName(), depth, batch, direction, filterStr));
                batch.clear();
                if (++cnt >= TRAVERSAL_BATCH_COUNT) {
                    return all;
                }
            }
        }
        if (batch.size() > 0) {
            all.addAll(graphRepo.traversalGraph(
                    StringUtils.join(edgeCollection.getVertexCollection(), ","),
                    edgeCollection.getTableName(), depth, batch, direction, filterStr));
        }
        return all;
    }

    private String buildGraphFilter(EdgeCollection edgeCollection, GraphOptions options) {
        StringBuilder sb = new StringBuilder(" FILTER True ");

        if (options != null && options.getFilter() != null && options.getFilter().getEdge() != null) {
            Map<String, Object> edgeFilters = options.getFilter().getEdge();
            String filterName = edgeCollection.getFilterName();
            Map<String, Object> edgeProp = (Map<String, Object>) edgeFilters.get(filterName);
            if (edgeProp != null) {
                // tradable_share需要特殊处理
                if (edgeCollection == EdgeCollection.TRADABLE_SHARE) {
                    String filterStr = generateTradableShareFilter(edgeProp, options.getEdges());
                    sb.append(filterStr);
                } else {
                    for (Map.Entry<String, Object> item : edgeProp.entrySet()) {
                        String filterStr = generateEdgeDetail(edgeCollection, item.getKey(), item.getValue());
                        sb.append(filterStr);
                    }
                }
            }
            sb.append("\n");
        }

        if (options != null && options.getFilter() != null && options.getFilter().getVertex() != null) {
            String[] defaultNodes = edgeCollection.getVertexCollection();
            Map<String, Object> nodeFilters = options.getFilter().getVertex();
            for (String node : defaultNodes) {
                Map<String, Object> nodeProp = (Map<String, Object>) nodeFilters.get(node);
                if (nodeProp != null) {
                    sb.append(String.format(" AND ((NOT IS_SAME_COLLECTION('%s', v._id)) OR (IS_SAME_COLLECTION('%s', v._id) ", node, node));
                    for (Map.Entry<String, Object> item : nodeProp.entrySet()) {
                        String filterStr = generateNodeDetail(node, item.getKey(), item.getValue());
                        sb.append(filterStr);
                    }
                    sb.append("))\n");
                }
            }
        }

        return sb.toString();
    }

    private String generateNodeDetail(String node, String key, Object value) {
        StringBuilder sb = new StringBuilder();
        switch (node) {
            case "Company":
                if ("capital".equals(key)) {
                    sb.append(rangeBuilder("TO_NUMBER(v.capital)", value, 1.0));
                } else if ("operation_startdate".equals(key)) {
                    sb.append(rangeBuilder("v.operation_startdate", value, null));
                }
                break;
            case "Bid_detail":
                if ("publish_time".equals(key)) {
                    sb.append(rangeBuilder("v.publish_time", value, null));
                }
                break;
            case "Judgement_wenshu":
                if ("case_date".equals(key)) {
                    sb.append(rangeBuilder("v.case_date", value, null));
                }
                break;
            case "Court_bulletin_doc":
                if ("court_time".equals(key)) {
                    sb.append(rangeBuilder("v.court_time", value, null));
                }
                break;
            case "Judge_process":
                if ("filing_date".equals(key)) {
                    sb.append(rangeBuilder("v.filing_date", value, null));
                }
                break;
            case "Court_announcement_doc":
                if ("bulletin_date".equals(key)) {
                    sb.append(rangeBuilder("v.bulletin_date", value, null));
                }
                break;
            default:
                break;
        }
        return sb.toString();
    }

    private String generateEdgeDetail(EdgeCollection edgeCollection, String key, Object value) {
        StringBuilder sb = new StringBuilder();
        if (edgeCollection == EdgeCollection.INVEST) {
            if ("invest_amount".equals(key)) {
                sb.append(rangeBuilder(" TO_NUMBER(e.invest_amount) ", value, 1.0));
            } else if ("invest_ratio".equals(key)) {
                sb.append(rangeBuilder(" e.invest_amount / document(e._to).capital ", value, 0.01));
            }
        } else if (edgeCollection == EdgeCollection.SHAREHOLDER) {
            if ("shareholder_ratio".equals(key)) {
                sb.append(rangeBuilder(" e.invest_amount / document(e._to).capital ", value, 0.01));
            } else if ("shareholder_type".equals(key)) {
                sb.append(generateShareholderStr(value));
            }
        } else if (edgeCollection == EdgeCollection.INDIRECT_INVEST) {
            if ("actual_control_ratio".equals(key)) {
                sb.append(rangeBuilder(" TO_NUMBER(e.actual_control_ratio) ", value, 0.01));
            }
        } else if (edgeCollection == EdgeCollection.OFFICER) {
            if ("position".equals(key)) {
                List<String> data = getListValue(value);
                if (data != null && data.size() > 0) {
                    if (data.contains("法定代表人") && !data.contains("法人")) {
                        data.add("法人");
                    }
                    sb.append(" AND (");
                    String[] tmp = new String[data.size()];
                    for (int i = 0; i < data.size(); i++) {
                        if (data.get(i).equals("其他高管")) {
                            tmp[i] = String.format("(NOT CONTAINS(e.position, '%s') AND NOT CONTAINS(e.position, '%s')" +
                                    " AND NOT CONTAINS(e.position, '%s') AND NOT CONTAINS(e.position, '%s'))", "董事", "监事", "法定代表人", "法人");
                        } else {
                            tmp[i] = String.format("CONTAINS(e.position,'%s')", data.get(i));
                        }
                    }
                    sb.append(StringUtils.join(tmp, " OR "));
                    sb.append(")");
                }
            }
        } else if (edgeCollection == EdgeCollection.SUE) {
            if ("type".equals(key)) {
                List<String> data = getListValue(value);
                if (data != null && data.size() > 0) {
                    sb.append(" AND (");
                    String[] tmp = new String[data.size()];
                    for (int i = 0; i < data.size(); i++) {
                        tmp[i] = String.format("e.type == '%s'", data.get(i));
                    }
                    sb.append(StringUtils.join(tmp, " OR "));
                    sb.append(")");
                }
            }
        }
        return sb.toString();
    }

    private String generateTradableShareFilter(Map<String, Object> filterMap, List<EdgesOption> edges) {
        StringBuilder sb = new StringBuilder();
        boolean hasInvest = false;
        for (EdgesOption edgesOption : edges) {
            if (edgesOption.getCategory().equals("invest") && edgesOption.getVisible()) {
                hasInvest = true;
            }
        }
        if (filterMap.containsKey("tradable_type")) {
            sb.append(generateShareholderStr(filterMap.get("tradable_type")));
        }
        Map<String, Object> shareholderFilter = (Map<String, Object>) filterMap.get("shareholder_ratio");
        Map<String, Object> investFilter = (Map<String, Object>) filterMap.get("invest_ratio");
        if (shareholderFilter != null && shareholderFilter.size() > 0 && investFilter != null && investFilter.size() > 0) {
            // or 条件
            StringBuilder sub = new StringBuilder(" AND (");
            String f1 = "(" + rangeBuilder(" TO_NUMBER(RTRIM(e.total_stake_distribution,'%')) ", shareholderFilter, 1.0)
                    .replaceFirst("AND", "") + " AND e._to == start)";
            String f2 = "(" + rangeBuilder(" TO_NUMBER(RTRIM(e.total_stake_distribution,'%')) ", investFilter, 1.0)
                    .replaceFirst("AND", "  ") + " AND e._from == start)";
            sub.append(f1).append(" OR ").append(f2).append(")");
            sb.append(sub);
        } else if (shareholderFilter != null && shareholderFilter.size() > 0) {
            StringBuilder sub = new StringBuilder(" AND (");
            String f1 = "(" + rangeBuilder(" TO_NUMBER(RTRIM(e.total_stake_distribution,'%')) ", shareholderFilter, 1.0)
                    .replaceFirst("AND", "") + " AND e._to == start)";
            if (hasInvest) {
                sub.append(f1).append(" OR ").append("(e._from == start))");
            } else {
                sub.append(f1).append(")");
            }
            sb.append(sub);
        } else if (investFilter != null && investFilter.size() > 0) {
            String f2 = "(" + rangeBuilder(" TO_NUMBER(RTRIM(e.total_stake_distribution,'%')) ", investFilter, 1.0)
                    .replaceFirst("AND", "  ") + " AND e._from == start)";
            sb.append(" AND ((e._to == start) OR ").append(f2).append(")");
        }
        return sb.toString();
    }

    private String generateShareholderStr(Object value) {
        StringBuilder sb = new StringBuilder();
        List<String> data = getListValue(value);
        if (data != null && data.size() > 0) {
            sb.append(" AND (");
            String[] tmp = new String[data.size()];
            for (int i = 0; i < data.size(); i++) {
                tmp[i] = String.format("is_same_collection('%s',e._from)", data.get(i));
            }
            sb.append(StringUtils.join(tmp, " OR "));
            sb.append(")");
        }
        return sb.toString();
    }

    private String rangeBuilder(String targetStr, Object value, Double ratio) {
        Map<String, Object> data = getMapValue(value);
        if (data != null) {
            StringBuilder sb = new StringBuilder();
            if (data.containsKey("min")) {
                if (ratio != null) {
                    sb.append(" AND ").append(targetStr).append(" >= ").append(Double.parseDouble(data.get("min").toString()) * ratio);
                } else {
                    sb.append(" AND ").append(targetStr).append(" >= '").append(data.get("min").toString()).append("'");
                }
            }
            if (data.containsKey("max")) {
                if (ratio != null) {
                    sb.append(" AND ").append(targetStr).append(" <= ").append(Double.parseDouble(data.get("max").toString()) * ratio);
                } else {
                    sb.append(" AND ").append(targetStr).append(" <= '").append(data.get("max").toString()).append("'");
                }
            }
            return sb.toString();
        }
        return "";
    }

    private Map<String, Object> getMapValue(Object value) {
        if (value == null) return null;
        if (!(value instanceof Map)) return null;
        return (Map<String, Object>) value;
    }

    private List<String> getListValue(Object value) {
        if (value == null) return null;
        if (!(value instanceof List)) return null;
        return (List<String>) value;
    }

}
