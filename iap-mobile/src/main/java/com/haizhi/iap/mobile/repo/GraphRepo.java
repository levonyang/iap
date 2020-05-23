package com.haizhi.iap.mobile.repo;

import com.google.common.collect.Maps;
import com.haizhi.iap.common.utils.MapBuilder;
import com.haizhi.iap.mobile.bean.normal.GraphEdge;
import com.haizhi.iap.mobile.bean.normal.GraphQuery;
import com.haizhi.iap.mobile.component.GraphWS;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * Created by chenbo on 17/2/22.
 */
@Slf4j
@Repository
@SuppressWarnings("unchecked")
public class GraphRepo
{
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
                "for person in persons " +
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

    //单表查询获取所有路径 by tableName depth id
    public List<Map<String, Object>> getPathBySingleCollection(String collection, int depth, String id) {
        String aql = "FOR v,e,p IN 1.." + depth + " ANY '" + id + "' " + collection + " return p";

        log.debug("单表查询-获取所有路径 AQL :" + aql);
        return executeAQL(new GraphQuery(aql, Collections.EMPTY_MAP));
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

    public List<Map<String, Object>> traversalGraph(String withDocuments, String tableName, int depth, Set<String> startVertices,
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
        String aql = String.format("with %s\n" +
                "for start in [%s]\n" +
                "for v, e in 1..@depth\n" +
                "%s start\n" +
                "%s\n" +
                "options {bfs:true,uniqueVertices:'path'}\n" +
                " %s \n" +
                " %s \n" +
                "return {\"vertexes\":v, \"edges\":e}", withDocuments, startArray, direction.getArangoMarker(), StringUtils.join(edgeTables, ", "), filterStr, limit);

        Map<String, Object> bindVars = Maps.newHashMap();
        bindVars.put("depth", depth);
        if(!CollectionUtils.isEmpty(params))
            bindVars.putAll(params);

        return executeAQLToGetMap(new GraphQuery(aql, bindVars, 10000));
    }

    /**
     * @author thomas
     * traverse操作，支持额外的AQL参数，返回计数count
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
    public Long traversalGraphToCount(String withDocuments, Collection<String> edgeTables, int depth, Set<String> startVertices,
                                                                                               GraphEdge.Direction direction, String filterStr, Map<String, Object> params, Integer offset, Integer size) {
        if(CollectionUtils.isEmpty(edgeTables)) return 0L;

        filterStr = StringUtils.trimToEmpty(filterStr);
        String startArray = "'" + StringUtils.join(startVertices.toArray(), "','") + "'";
        String limit = offset != null && offset >= 0 && size != null && size > 0 ? String.format("LIMIT %d, %d", offset, size) : "";
        String aql = String.format("with %s\n" +
                "for start in [%s]\n" +
                "for v, e in 1..@depth\n" +
                "%s start\n" +
                "%s\n" +
                "options {bfs:true,uniqueVertices:'path'}\n" +
                " %s \n" +
                " %s \n" +
                "return {\"vertexes\":v, \"edges\":e}", withDocuments, startArray, direction.getArangoMarker(), StringUtils.join(edgeTables, ", "), filterStr, limit);

        Map<String, Object> bindVars = Maps.newHashMap();
        bindVars.put("depth", depth);
        if(!CollectionUtils.isEmpty(params))
            bindVars.putAll(params);

        return executeAQLToCount(new GraphQuery(aql, bindVars, 10000, true));
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
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * @author thomas
     * 执行AQL，返回Map信息 [{vertexes: {}, edges: {}}
     *
     * @param graphQuery
     * @return
     */
    private List<Map<String, Map<String, Object>>> executeAQLToGetMap(GraphQuery graphQuery) {
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
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * @author thomas
     * 执行AQL，返回count信息
     *
     * @param graphQuery
     * @return
     */
    private Long executeAQLToCount(GraphQuery graphQuery) {
        try {
            Map<String, Object> resp = graphWS.query(graphQuery);
            if (resp.get("error") instanceof Boolean && !(boolean) resp.get("error")) {
                if (resp.get("result") instanceof List) {
                    Long cnt = null;
                    try {
                        cnt = Long.parseLong(resp.get("count").toString());
                    } catch (Exception ignore) {}
                    return cnt;
                }
            }
        } catch (Exception e) {
            log.error("error aql : {}", graphQuery.getQuery());
            log.error(e.getMessage(), e);
        }
        return 0L;
    }
}
