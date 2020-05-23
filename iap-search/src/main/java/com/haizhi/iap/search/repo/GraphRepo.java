package com.haizhi.iap.search.repo;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.haizhi.iap.common.exception.ServiceAccessException;
import com.haizhi.iap.common.utils.MapBuilder;
import com.haizhi.iap.search.controller.GraphFoxxWS;
import com.haizhi.iap.search.controller.GraphWS;
import com.haizhi.iap.search.controller.model.GraphQuery;
import com.haizhi.iap.search.controller.model.GraphReq;
import com.haizhi.iap.search.controller.model.PageArangoParam;
import com.haizhi.iap.search.controller.model.PageResult;
import com.haizhi.iap.search.controller.model.ReqGuaranteeOrTransfer;
import com.haizhi.iap.search.exception.SearchException;
import com.haizhi.iap.search.model.GraphEdge;
import com.haizhi.iap.search.service.impl.FoxxAdapterService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by chenbo on 17/2/22.
 */
@Slf4j
@Repository
@SuppressWarnings("unchecked")
public class GraphRepo {

    @Setter
    @Autowired
    GraphFoxxWS graphFoxxWS;

    @Autowired
    FoxxAdapterService foxxAdapterService;

    @Setter
    @Autowired
    GraphWS graphWS;

    private final static String COLL_COMPANY = "Company";
    private final static String COLL_PERSON = "Person";

    private final static List<String> outerInvestEdges = Arrays.asList("invest", "tradable_share");

    public Map<String, Object> getCompanyByName(String company) {
        String aql = "With Company FOR doc IN " + COLL_COMPANY + " FILTER doc.name == @name LIMIT 1 return doc ";
        Map<String, Object> bindVars = new MapBuilder().put("name", company).build();

        List<Map<String, Object>> res = executeAQL(new GraphQuery(aql, bindVars));
        return res.size() > 0 ? res.get(0) : null;
    }

    public Map<String, Object> getGroup(String groupId) {
        String aql = "" +
                "LET neighbours  = APPEND(" +
                "   FOR docx IN Company FILTER docx.`group_id` == @group_id  limit 20  RETURN docx," +
                "   FOR docx IN Person FILTER docx.`group_id` == @group_id  limit 20  RETURN docx" +
                ")" +
                "LET edges_within_neighbours = (" +
                "   FOR docx IN neighbours" +
                "       FOR v,e,p in 1..1 OUTBOUND docx invest, officer, guarantee, suspected" +
                "           FILTER v in neighbours" +
                "           RETURN e" +
                ")" +
                "RETURN {" +
                "   neighbours: neighbours, " +
                "   edges_within_neighbours: edges_within_neighbours" +
                "}";
        Map<String, Object> bindVars = new MapBuilder().put("group_id", groupId).build();
        List<Map<String, Object>> res = executeAQL(new GraphQuery(aql, bindVars));
        return res.size() > 0 ? res.get(0) : null;
    }

    public List<Map<String, Object>> getInvestAndOfficer(String person) {
        String aql = "WITH Company, Person FOR v, e, p IN 1 ANY @startVertex invest, tradable_share, officer" +
                    " RETURN p";
        Map<String, Object> bindVars = new MapBuilder().put("startVertex", person).build();
        return executeAQL(new GraphQuery(aql, bindVars));
    }

    public List<Map<String, Object>> getMergeSuggested(String person) {
        String aql = "WITH Company, Person FOR v, e, p IN 1 ANY @startVertex person_merge_suggest" +
                " RETURN e";
        Map<String, Object> bindVars = new MapBuilder().put("startVertex", person).build();
        return executeAQL(new GraphQuery(aql, bindVars));
    }

    public Map<String, Object> fetchDocument(String entityId) {
        if(StringUtils.isEmpty(entityId)){
            return null;
        }
        String aql = "return document(@doc_id)";
        Map<String, Object> bindVars = new MapBuilder().put("doc_id", entityId).build();
        List<Map<String, Object>> res = executeAQL(new GraphQuery(aql, bindVars));
        return res.size() > 0 ? res.get(0) : null;
    }

    public List<Map<String, Object>> getFamiliar(String id) {
        String aql = "WITH Company, Person FOR v, e, p IN 1..@depth ANY @start_vertex invest,officer" +
                "                FILTER v._id != @start_vertex" +
                "                FILTER IS_SAME_COLLECTION(\"" + COLL_PERSON + "\", v)" +
                "                LIMIT 10" +
                "                RETURN p ";
        Map<String, Object> bindVars = new MapBuilder().put("start_vertex", id)
                .put("depth", 2)
                .build();
        return executeAQL(new GraphQuery(aql, bindVars));
    }


    public Map<String, Object> getShortestPath(String companyA, String companyB, GraphReq req) {
        try {
            return foxxAdapterService.shortestPath(companyA, companyB, req);
        } catch (Exception ex) {
            log.error("{}", ex);
            throw new ServiceAccessException(SearchException.GRAPH_SERVER_ERROR);
        }
    }

    public Map<String, Object> checkGuaranteeCircle(String companyA, String companyB) {
        try {
            return foxxAdapterService.checkGuaranteeCircle(companyA, companyB);
        } catch (Exception ex) {
            log.error("{}", ex);
            throw new ServiceAccessException(SearchException.GRAPH_SERVER_ERROR);
        }
    }

    public Map<String, Object> findCommonParent(String companyA, String companyB) {
        try {
            return foxxAdapterService.findCommonParent(companyA, companyB);
        } catch (Exception ex) {
            log.error("{}", ex);
            throw new ServiceAccessException(SearchException.GRAPH_SERVER_ERROR);
        }
    }

    public Map<String, Object> traversal(String id, GraphReq req) {
        try {
            return foxxAdapterService.traversal(id, req);
        } catch (Exception ex) {
            log.error("{}", ex);
            throw new ServiceAccessException(SearchException.GRAPH_SERVER_ERROR);
        }
    }

    public List<Map<String, Object>> getMergeSuggestedList(String personId) {
        String aql = "WITH Company, Person FOR v, e, p IN @depth ANY @start_vertex person_merge_suggest" +
                " return v";
        Map<String, Object> bindVars = new MapBuilder()
                .put("start_vertex", personId)
                .put("depth", 1)
                .build();
        return executeAQL(new GraphQuery(aql, bindVars));
    }

    public Map<String, Object> getPersonById(String id) {
        String aql = "WITH Person FOR doc IN " + COLL_PERSON + " FILTER doc._id == @person_id LIMIT 1 return doc ";
        Map<String, Object> bindVars = new MapBuilder().put("person_id", id).build();
        List<Map<String, Object>> res = executeAQL(new GraphQuery(aql, bindVars));
        return res.size() > 0 ? res.get(0) : null;
    }

    public List<Map<String, Object>> getActualControlMan(String companyName) {
        Map<String, Object> company = getCompanyByName(companyName);
        if (company != null && company.containsKey("_id")) {
            String companyId = (String) company.get("_id");
            String aql = "WITH Company, Person FOR edge IN actual_controller \n" +
                    "FILTER edge._to == @companyId return {\"controller\": DOCUMENT(edge._from),\"edge\":edge}";
            Map<String, Object> bindVars = new MapBuilder().put("companyId", companyId).build();
            return executeAQL(new GraphQuery(aql, bindVars));
        }
        return Collections.EMPTY_LIST;
    }

    public List<Map<String, Object>> getOfficerOfCompany(String companyName) {
        Map<String, Object> company = getCompanyByName(companyName);
        if (company != null && company.containsKey("_id")) {
            String companyId = (String) company.get("_id");
            String aql = "WITH Company, Person For doc in officer filter doc._to == @companyId return {\"position\" : doc.position, \"person\":DOCUMENT(doc._from)}";
            Map<String, Object> bindVars = new MapBuilder().put("companyId", companyId).build();
            return executeAQL(new GraphQuery(aql, bindVars));
        }
        return null;
    }

    public List<Map<String, Object>> getInvestOfCompany(String companyName) {
        Map<String, Object> company = getCompanyByName(companyName);
        if (company != null && company.containsKey("_id")) {
            String companyId = (String) company.get("_id");
            String aql = "WITH Company, Person For doc in invest filter doc._to == @companyId return {\"invest\" : doc, \"person\":DOCUMENT(doc._from)}";
            Map<String, Object> bindVars = new MapBuilder().put("companyId", companyId).build();
            return executeAQL(new GraphQuery(aql, bindVars));
        }
        return null;
    }

    public List<Map<String, Object>> getTradableShareOfCompany(String companyName) {
        Map<String, Object> company = getCompanyByName(companyName);
        if (company != null && company.containsKey("_id")) {
            String companyId = (String) company.get("_id");
            String aql = "WITH Company, Person For doc in tradable_share filter doc._to == @companyId return {\"shareholding_ratio\" : doc.total_stake_distribution, \"person\":DOCUMENT(doc._from)}";
            Map<String, Object> bindVars = new MapBuilder().put("companyId", companyId).build();
            return executeAQL(new GraphQuery(aql, bindVars));
        }
        return null;
    }

    public List<Map<String, Object>> getConcert(String companyId) {
        if (companyId == null) {
            return null;
        }
        String aql = "WITH Company, Person FOR v, e, p IN 1..@depth ANY @startVertex concert " +
                " RETURN p";
        Map<String, Object> bindVars = new MapBuilder()
                .put("depth", 1)
                .put("startVertex", companyId).build();
        return executeAQL(new GraphQuery(aql, bindVars));
    }

    public List<Map<String, Object>> getRelation(String coll, String from, String to, boolean hasDirection) {
        if (coll == null || (from == null && to == null)) {
            return null;
        }
        String aql;

        if (hasDirection) {
            if (from == null) {
                aql = "WITH Company, Person For doc IN " + coll + "" +
                        " FILTER doc._to == @to " +
                        " return doc";
            } else if (to == null) {
                aql = "WITH Company, Person For doc IN " + coll + "" +
                        " FILTER doc._from == @from " +
                        " return doc";
            } else {
                aql = "WITH Company, Person For doc IN " + coll + "" +
                        " FILTER doc._from == @from AND doc._to == @to " +
                        " LIMIT 10" +
                        " return doc";
            }
        } else {
            aql = "WITH Company, Person For doc IN " + coll + "" +
                    " FILTER (doc._from == @from AND doc._to == @to) OR (doc._from == @to AND doc._to == @from) " +
                    " LIMIT 10" +
                    " return doc";
        }
        Map<String, Object> bindVars = new MapBuilder()
                .put("from", from)
                .put("to", to).build();
        return executeAQL(new GraphQuery(aql, bindVars));
    }

    public List<Map<String, Object>> getRelationOfConcert(String from, String to, String target, boolean hasDirection) {
        try {
            if (from == null || to == null) {
                return null;
            }
            if(target.equals("")){
                target = "all";
            }
            String aql;

            if (hasDirection) {
                aql = "WITH Company, Person For doc IN concert" +
                        " FILTER doc._from == @from AND doc._to == @to AND doc.target=@target" +
                        " LIMIT 10" +
                        " return doc";
            } else {
                aql = "WITH Company, Person For doc IN concert" +
                        " FILTER (doc._from == @from AND doc._to == @to AND doc.target==@target) OR (doc._from == @to AND doc._to == @from AND doc.target==@target) " +
                        " LIMIT 10" +
                        " return doc";
            }
            Map<String, Object> bindVars = new MapBuilder()
                    .put("from", from)
                    .put("to", to)
                    .put("target", target)
                    .build();
            Map<String, Object> resp = graphWS.query(new GraphQuery(aql, bindVars));
            if (resp.get("error") instanceof Boolean && !(boolean) resp.get("error")) {
                if (resp.get("result") instanceof List && ((List) resp.get("result")).size() > 0) {
                    return ((List<Map<String, Object>>) resp.get("result"));
                }
            }
        } catch (Exception ex) {
            log.error("{}", ex);
            throw new ServiceAccessException(SearchException.GRAPH_SERVER_ERROR);
        }
        return Collections.EMPTY_LIST;
    }

    public List<Map<String, Object>> getCompanyInvestOrOfficer(String companyId, int depth, int limit) {
        String aql = "WITH Company, Person For v, e, p in 1 .. @depth ANY @companyId invest, officer " +
                " limit @limit " +
                " return p ";
        Map<String, Object> bindVars = new MapBuilder().put("companyId", companyId)
                .put("depth", depth).put("limit", limit).build();
        return executeAQL(new GraphQuery(aql, bindVars));
    }

    public List<Map<String, Object>> getPersonInvestOrOfficer(String personId, int personMergeDepth, int depth, int limit) {
        String aql = "WITH Company, Person let persons = ( " +
                "    for v, e, p in 1 .. @personMergeDepth ANY @personId person_merge " +
                "        return v._id " +
                ") " +
                " LET personsAll = push(persons,@personId) "+
                "for person in personsAll " +
                "    for v, e, p in 1 .. @depth ANY person invest, officer " +
                "        limit @limit " +
                "        return p";
        Map<String, Object> bindVars = new MapBuilder().put("personId", personId)
                .put("personMergeDepth", personMergeDepth).put("depth", depth).put("limit", limit).build();
        return executeAQL(new GraphQuery(aql, bindVars));
    }

    /**
     * 获取企业派系
     *
     * @param companyId
     * @return
     */
    public List<Map<String, Object>> getGroupOfCompanyById(String companyId) {
        String aql = "WITH Company, Person For doc in company_group filter doc._from == @companyId return DOCUMENT(doc._to)";
        Map<String, Object> bindVars = new MapBuilder().put("companyId", companyId).build();
        return executeAQL(new GraphQuery(aql, bindVars));
    }

    public List<Map<String, Object>> getHolding(String companyId) {
        String aql = "WITH Company, Person For doc in control_shareholder filter doc._to == @companyId return DOCUMENT(doc._from)";
        Map<String, Object> bindVars = new MapBuilder().put("companyId", companyId).build();
        return executeAQL(new GraphQuery(aql, bindVars));
    }

    /**
     * 获取公司对外投资
     *
     * @param companyId
     * @return
     */
    public List<Map> getOuterInvest(String companyId) {
        List<Map> result = Lists.newArrayList();

        try {
            for (String edge : outerInvestEdges) {
                String aql = "WITH Company, Person For doc in " + edge + " filter doc._from == @companyId return {'invest': doc, 'entity': DOCUMENT(doc._to)}";
                Map<String, Object> bindVars = new MapBuilder().put("companyId", companyId).build();
                Map<String, Object> resp = graphWS.query(new GraphQuery(aql, bindVars));

                if (resp.get("error") instanceof Boolean && !(boolean) resp.get("error")) {
                    if (resp.get("result") instanceof List) {
                        for (Map<String, Object> map : (List<Map>) resp.get("result")) {
                            Map data = Maps.newHashMap();
                            if (map != null && map.get("invest") != null && map.get("invest") instanceof Map) {
                                data.put("invest_amount", ((Map) map.get("invest")).get("invest_amount"));
                                data.put("invest_amount_unit", ((Map) map.get("invest")).get("invest_amount_unit"));
                            }
                            if (map != null && map.get("entity") != null && map.get("entity") instanceof Map) {
                                data.putAll((Map) map.get("entity"));
                            }
                            if (data.keySet().size() > 0) {
                                result.add(data);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.error("{}", ex);
            throw new ServiceAccessException(SearchException.GRAPH_SERVER_ERROR);
        }
        return result;
    }

    public PageArangoParam getPageArangoParam(ReqGuaranteeOrTransfer req) {
        StringBuffer aql = new StringBuffer("FOR doc IN " + req.getType());
        StringBuffer aqlCount = new StringBuffer("RETURN LENGTH(");

        Map<String, Object> bindVars = new HashMap<>();

        String sort = " SORT ";
        String filter = " FILTER ";

        int i = 0;

        for (Map<String, Object> condition : req.getConditionList()) {
            String paramMin = "param" + i;
            i++;
            String paramMax = "param" + i;
            i++;

            String conditionMIN = "";
            String conditionMAX = "";
            if (!StringUtils.isEmpty((String) condition.get("min"))) {
                bindVars.put(paramMin, condition.get("min"));
                conditionMIN = " >= @" + paramMin + " AND ";
            }
            if (!StringUtils.isEmpty((String) condition.get("max"))) {
                bindVars.put(paramMax, condition.get("max"));
                conditionMAX = " <= @" + paramMax + " AND ";
            }

            String field = "";
            if (!StringUtils.isEmpty((String) condition.get("field"))) {
                field = condition.get("field").toString();
            }

            //区间
            if ("section".equals(condition.get("conditionType"))) {
                String[] fieldList = field.split(",");

                if (!StringUtils.isEmpty(conditionMIN)) {
                    filter = filter + "doc." + fieldList[0] + conditionMIN;
                }
                if (!StringUtils.isEmpty(conditionMAX)) {
                    filter = filter + "doc." + fieldList[1] + conditionMAX;
                }

                continue;
            } else if ("range".equals(condition.get("conditionType"))) {
                if (!StringUtils.isEmpty(conditionMIN)) {
                    filter = filter + "doc." + field + conditionMIN;
                }
                if (!StringUtils.isEmpty(conditionMAX)) {
                    filter = filter + "doc." + field + conditionMAX;
                }
            } else if ("list".equals(condition.get("conditionType"))) {
                for (Map.Entry<String, Object> entry : condition.entrySet()) {
                    if ("conditionType".equals(entry.getKey())) {
                        continue;
                    }
                    Object value = null;
                    filter = filter + " doc." + entry.getKey() + " IN [";
                    for (Object itemList : (List) entry.getValue()) {
                        String param = "param" + i;
                        filter = filter + "@" + param + ",";
                        try {
                            value = Double.valueOf(itemList.toString());
                        } catch (Exception e) {
                            value = itemList;
                        }
                        bindVars.put(param, value);
                        i++;
                    }
                    filter = filter.substring(0, filter.length() - 1);
                    filter = filter + "] AND";
                }
                continue;
            } else {
                //default is map
                Iterator<String> keySet = condition.keySet().iterator();
                String key = null;
                while (keySet.hasNext()) {
                    key = keySet.next();
                    if ("conditionType".equals(key)) {
                        continue;
                    }
                    filter = filter + " doc." + key + " == @param" + i + " AND ";
                    bindVars.put("param" + i, condition.get(key));
                    i++;
                }
                continue;
            }

            String sortType = (String) condition.get("sort");
            if (!StringUtils.isEmpty(sortType) && !"default".equalsIgnoreCase(sortType.toString())) {
                sort = sort + " doc." + field + " " + sortType + ",";
            }

        }

        if (sort.length() != 6) {
            sort = sort.substring(0, sort.length() - 1);
            aql.append(sort);
        }
        if (req.isTwoWay()) {
            filter = filter + " ( (doc._from == '" + req.getFrom() + "' AND doc._to == '" + req.getTo()
                    + "') OR (doc._from == '" + req.getTo() + "' AND doc._to == '" + req.getFrom() + "') )";
        } else {
            filter = filter + " doc._from == '" + req.getFrom() + "' AND doc._to == '" + req.getTo() + "' ";
        }
        aql.append(filter);

        aqlCount.append(aql.toString() + " RETURN doc)");

        aql.append("LIMIT " + req.getOffset() + "," + req.getCount());
        aql.append(" RETURN doc");

        log.debug("担保或转账关系详情 AQL : " + aql.toString());
        log.debug("担保或转账关系详情 COUNT ： " + aqlCount.toString());
        log.debug("担保或转账关系详情参数 ： " + bindVars.toString());

        return new PageArangoParam(aql.toString(), aqlCount.toString(), bindVars);
    }

    public PageResult getArangoListByPageMoreCondition(PageArangoParam param) {
        try {
            PageResult result = new PageResult();

            Map<String, Object> resp = graphWS.query(new GraphQuery(param.getAql(), param.getBindVars()));
            if (resp.get("error") instanceof Boolean && !(boolean) resp.get("error")) {
                if (resp.get("result") instanceof List) {
                    result.setList((List<Map<String, Object>>) resp.get("result"));
                }
            }

            Map<String, Object> respCount = graphWS.query(new GraphQuery(param.getAqlCount(), param.getBindVars()));
            if (respCount.get("error") instanceof Boolean && !(boolean) respCount.get("error")) {
                if (respCount.get("result") instanceof List) {
                    result.setTotal(((List<Integer>) respCount.get("result")).get(0));
                }
            }
            return result;
        } catch (Exception ex) {
            log.error("获取担保关系 或 转账 详细信息时出错 ！", ex);
            ex.printStackTrace();
        }
        return null;
    }

    //单表查询获取所有路径 by tableName depth id
    public List<Map<String, Object>> getPathBySingleCollection(String collection, int depth, String id) {
        String aql = "FOR v,e,p IN 1.." + depth + " ANY '" + id + "' " + collection + " return p";

        log.debug("单表查询-获取所有路径 AQL :" + aql);
        return executeAQL(new GraphQuery(aql, Collections.EMPTY_MAP));
    }

    public Map<String, Object> findPathByIds(GraphReq req) {
        return findPathByIds(false, req);
    }

    public Map<String, Object> findPathByIds(boolean bidirectional, GraphReq req) {
        return findPathByIds(bidirectional, false, req);
    }

    public Map<String, Object> findPathByIds(boolean bidirectional, boolean stopIfFound, GraphReq req) {
        List<String> fromList = req.getFromList();
        List<String> depthList = req.getDepthList();
        String to = req.getTo();
        Map<String, Map<String, Object>> v = Maps.newHashMap();
        Map<String, Map<String, Object>> e = Maps.newHashMap();
        for (int i = 0; i < fromList.size(); i++) {
            String start = fromList.get(i);
            if (depthList != null && i < depthList.size()) {
                req.getOptions().setMaxLength(Integer.valueOf(depthList.get(i)));
            }
            Map<String, Object> res = foxxAdapterService.shortestPathById(start, to, req, bidirectional, stopIfFound);
            ((List<Map<String, Object>>) res.get("vertexes")).stream().forEach(one -> {
                String id = (String) one.get("_id");
                if (!v.containsKey(id)) {
                    v.put(id, one);
                }
            });
            ((List<Map<String, Object>>) res.get("edges")).stream().forEach(one -> {
                String id = (String) one.get("_id");
                if (!e.containsKey(id)) {
                    e.put(id, one);
                }
            });
        }
        Map<String, Object> out = Maps.newHashMap();
        out.put("vertexes", Lists.newArrayList(v.values()));
        out.put("edges", Lists.newArrayList(e.values()));
        return out;
    }

    public List<Map<String, Object>> findMergedPerson(String startVertex) {
        String startArray = "'" + startVertex + "'";
        // person_merge是星型结构的，所以depth最大是2
        String aql = String.format("with Person\n" +
                "for start in [%s]\n" +
                "for v, e in 1..2 ANY start person_merge\n" +
                "return {\"vertexes\":v, \"edges\":e}", startArray);

        return executeAQL(new GraphQuery(aql, Maps.newHashMap()));
    }

    public List<Map<String, Object>> findMergedPerson(Set<String> startVertices) {
        String startArray = "'" + StringUtils.join(startVertices.toArray(), "','") + "'";
        // person_merge是星型结构的，所以depth最大是2
        String aql = String.format("with Person\n" +
                "for start in [%s]\n" +
                "for v, e in 1..2 ANY start person_merge\n" +
                "return {\"vertexes\":v, \"edges\":e}", startArray);

        return executeAQL(new GraphQuery(aql, Maps.newHashMap()));
    }

    /**
     * @author thomas
     * 查找person_merge表，找到可融合的自然人
     *
     * @param startVertices
     * @return
     */
    public List<Map<String, Map<String, Object>>> findMergedPersons(Set<String> startVertices) {
        // person_merge是星型结构的，所以depth最大是2
        String aql = String.format("WITH Person\n" +
                "FOR start IN ['%s']\n" +
                "FOR v, e IN 1..2 ANY start person_merge\n" +
                "RETURN {\"vertexes\":v, \"edges\":e}", StringUtils.join(startVertices, "','"));

        return executeAQLToGetMap(new GraphQuery(aql, new HashMap<>()));
    }

    public List<Map<String, Object>> traversalGraph(String withDocuments,String tableName, int depth, Set<String> startVertices,
                                                    GraphEdge.Direction direction, String filterStr) {
        filterStr = StringUtils.trimToEmpty(filterStr);
        String startArray = "'" + StringUtils.join(startVertices.toArray(), "','") + "'";
        String aql = String.format("with %s\n" +
                "for start in [%s]\n" +
                "for v, e in 1..@depth %s start %s\n" +
                "options {bfs:true,uniqueVertices:'path'}\n" +
                " %s \n" +
                "return {\"vertexes\":v, \"edges\":e}", withDocuments, startArray, direction.getArangoMarker(), tableName, filterStr);

        Map<String, Object> bindVars = Maps.newHashMap();
        bindVars.put("depth", depth);

        return executeAQL(new GraphQuery(aql, bindVars, 10000));
    }

    /**
     * @author thomas
     * traverse操作，支持额外的AQL参数、且返回Map信息 [{vertexes: {}, edges: {}}
     *
     * @param withDocuments
     * @param edgeTables
     * @param depth
     * @param startVertices
     * @param direction
     * @param filterStr
     * @param params
     * @param offset
     * @param size
     * @return
     */
    public List<Map<String, Map<String, Object>>> traversalGraphWithParams(String withDocuments, Collection<String> edgeTables, int depth, Set<String> startVertices,
                                                    GraphEdge.Direction direction, String filterStr, Map<String, Object> params, Integer offset, Integer size) {
        if(CollectionUtils.isEmpty(edgeTables)) return Collections.emptyList();

        filterStr = StringUtils.trimToEmpty(filterStr);
        String startArray = "'" + StringUtils.join(startVertices.toArray(), "','") + "'";
        String limit = offset != null && offset >= 0 && size != null && size > 0 ? String.format("LIMIT %d, %d", offset, size) : "";
        String aql = String.format("WITH %s\n" +
                "FOR start IN [%s]\n" +
                "FOR v, e IN 1..@depth\n" +
                "%s start\n" +
                "%s\n" +
                "options {bfs:true,uniqueVertices:'path'}\n" +
                " %s \n" +
                " %s \n" +
                "RETURN {\"vertexes\":v, \"edges\":e}", withDocuments, startArray, direction.getArangoMarker(), StringUtils.join(edgeTables, ", "), filterStr, limit);

        Map<String, Object> bindVars = Maps.newHashMap();
        bindVars.put("depth", depth);
        if(!CollectionUtils.isEmpty(params))
            bindVars.putAll(params);

        return executeAQLToGetMap(new GraphQuery(aql, bindVars, 10000));
    }

    /**
     * @author caochao
     * traverse操作，支持额外的AQL参数、且返回Map信息 [{vertexes: {}, edges: {}}
     * 注意：此函数会接受一个必填的参数vertices。表示返回实体限定的在此数组之内，不在该集合中的数据将会被丢弃。
     * 并且：此函数将会对返回的Person根据PersonMerge做扩展，并将扩展范围限定在参数vertices之内
     * 最后：返回的实体和边id，如果是由PersonMerge扩展而来，实体将会把返回值id修改为vertices中对应实体的id，边会将_from或_to修改为vertices中对应实体的id。
     * @return
     */
    public List<Map<String, Map<String, Object>>> traversalGraphWithPersonMerge(String withDocuments, Collection<String> edgeTables, int depth, Set<String> startVertices,
                                                                           GraphEdge.Direction direction, String filterStr, Map<String, Object> params, Integer offset, Integer size) {
        if(CollectionUtils.isEmpty(edgeTables)) return Collections.emptyList();

        filterStr = StringUtils.trimToEmpty(filterStr);
        String startArray = "'" + StringUtils.join(startVertices.toArray(), "','") + "'";
        String limit = offset != null && offset >= 0 && size != null && size > 0 ? String.format("LIMIT %d, %d", offset, size) : "";
        String aql = String.format("WITH %s\n" +
                "FOR start IN [%s]\n" +
                "FOR v, e IN 1..@depth\n" +
                "%s start\n" +
                "%s\n" +
                "options {bfs:true,uniqueVertices:'path'}\n" +
                "let p =(FOR v1 IN 1..2 ANY v._id person_merge return v1._id)\n"+
                "let bothP = intersection(push(p,v._id),@vertices)\n"+
                "filter length(bothP) >0\n"+
                " %s \n" +
                " %s \n" +
                "RETURN {\"vertexes\": document(bothP[0]), \"edges\": merge(e,e._from==v._id?{\"_from\":bothP[0]}:{\"_to\":bothP[0]})}", withDocuments, startArray, direction.getArangoMarker(), StringUtils.join(edgeTables, ", "), filterStr, limit);

        Map<String, Object> bindVars = Maps.newHashMap();
        bindVars.put("depth", depth);
        if(!CollectionUtils.isEmpty(params))
            bindVars.putAll(params);

        return executeAQLToGetMap(new GraphQuery(aql, bindVars, 10000));
    }

    /**
     * @author thomas
     * traverse操作，返回路径信息（后续可能需要，勿删）
     *
     * @param withDocuments
     * @param tableName
     * @param depth
     * @param startVertices
     * @param direction
     * @param filterStr
     * @return
     */
    public List<Map<String, Object>> traversalGraphToGetPath(String withDocuments,String tableName, int depth, Set<String> startVertices,
                                                    GraphEdge.Direction direction, String filterStr) {
        filterStr = StringUtils.trimToEmpty(filterStr);
        String startArray = "'" + StringUtils.join(startVertices.toArray(), "','") + "'";
        String aql = String.format("WITH %s\n" +
                "FOR start IN [%s]\n" +
                "FOR v, e, p IN 1..@depth\n" +
                "%s start\n" +
                "%s\n" +
                "options {bfs:true,uniqueVertices:'path'}\n" +
                " %s \n" +
                "return p", withDocuments, startArray, direction.getArangoMarker(), tableName, filterStr);

        Map<String, Object> bindVars = Maps.newHashMap();
        bindVars.put("depth", depth);

        return executeAQL(new GraphQuery(aql, bindVars, 1000));
    }

    private List<Map<String, Object>> executeAQL(GraphQuery graphQuery) {
        try {
            Map<String, Object> resp = graphWS.query(graphQuery);
            if (resp.get("error") instanceof Boolean && !(boolean) resp.get("error")) {
                if (resp.get("result") instanceof List) {
                    return ((List<Map<String, Object>>) resp.get("result"));
                }
            }
        } catch (Exception e) {
            log.error("error aql : {}", graphQuery.getQuery());
            log.error(e.getMessage(), e);
            throw new ServiceAccessException(SearchException.GRAPH_SERVER_ERROR);
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * @author thomas
     * 执行AQL，返回Map信息 [{vertexes: {}, edges: {}}（后续可能需要，勿删）
     *
     * @param graphQuery
     * @return
     */
    private List<Map<String, Map<String, Object>>> executeAQLToGetMap(GraphQuery graphQuery) {
        //log.error("execute aql : {} " + JSON.toJSONString(graphQuery));
        try {
            Map<String, Object> resp = graphWS.query(graphQuery);
            if (resp.get("error") instanceof Boolean && !(boolean) resp.get("error")) {
                if (resp.get("result") instanceof List) {
                    return (List<Map<String, Map<String, Object>>>) resp.get("result");
                }
            }
        } catch (Exception e) {
            log.error("error aql : {}", graphQuery.getQuery());
            log.error(e.getMessage(), e);
            throw new ServiceAccessException(SearchException.GRAPH_SERVER_ERROR);
        }

        return Collections.EMPTY_LIST;
    }


}
