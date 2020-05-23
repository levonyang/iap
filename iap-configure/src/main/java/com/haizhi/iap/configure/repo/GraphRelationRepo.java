package com.haizhi.iap.configure.repo;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDatabase;
import com.arangodb.model.AqlQueryOptions;
import com.google.common.collect.Maps;
import com.haizhi.iap.configure.model.GraphRelation;
import com.haizhi.iap.configure.model.GraphResult;
import com.haizhi.iap.configure.model.SearchGraphParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
public class GraphRelationRepo {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ArangoDatabase arangoDatabase;

    private static final String saveGraphRelationSql = "insert into graph_relation(`name`, `source_config_id`, `comment`, `comment_name`, " +
            "`is_selected`, `is_show`, `is_multi`, `attrs`) values (?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE `name` = ?," +
            "`source_config_id` = ?, `comment` = ?, `comment_name` = ?, `is_selected` = ?, `is_show` = ?, `is_multi` = ?, `attrs` = ?, update_time = now()";

    private static final String updateGraphRelationByIdSql = "update graph_relation set `name` = ?, " +
            "`source_config_id` = ?, `comment` = ?, `comment_name` = ?, `is_selected` = ?, `is_show` = ?, `is_multi` = ?, `attrs` = ?, update_time = now()"
            + " where id = ?";
    public GraphRelation saveGraphRelation(GraphRelation graphRelation) {
        if (graphRelation.getId() != null) {
            jdbcTemplate.update(updateGraphRelationByIdSql, new Object[]{
                    graphRelation.getName(), graphRelation.getSourceConfigId(), graphRelation.getComment(), graphRelation.getCommentName(),
                    graphRelation.getIsSelected(), graphRelation.getIsShow(), graphRelation.getIsMulti(),
                    graphRelation.getAttrs(), graphRelation.getId()
            });
        } else {
            GeneratedKeyHolder holder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(saveGraphRelationSql, new String[]{"id"});
                ps.setString(1, graphRelation.getName());
                ps.setInt(2, graphRelation.getSourceConfigId());
                ps.setString(3, graphRelation.getComment());
                ps.setString(4, graphRelation.getCommentName());
                ps.setInt(5, graphRelation.getIsSelected());
                ps.setInt(6, graphRelation.getIsShow());
                ps.setInt(7, graphRelation.getIsMulti());
                ps.setString(8, graphRelation.getAttrs());

                ps.setString(9, graphRelation.getName());
                ps.setInt(10, graphRelation.getSourceConfigId());
                ps.setString(11, graphRelation.getComment());
                ps.setString(12, graphRelation.getCommentName());
                ps.setInt(13, graphRelation.getIsSelected());
                ps.setInt(14, graphRelation.getIsShow());
                ps.setInt(15, graphRelation.getIsMulti());
                ps.setString(16, graphRelation.getAttrs());
                return ps;
            }, holder);
            if (holder.getKeyList().size() == 1) {
                graphRelation.setId(holder.getKey().intValue());
            }
        }
        return graphRelation;
    }

    private static final String getGraphRelationSql = "select a.*, b.target_table from graph_relation a join datasource_config b on a.source_config_id = b.id";
    public List<GraphRelation> getGraphRelation(Integer graphId) {
        String sql = getGraphRelationSql;
        if (graphId != null) {
            sql += " where a.id = ?";
            return jdbcTemplate.query(sql, new Object[]{graphId}, new BeanPropertyRowMapper<GraphRelation>(GraphRelation.class));
        }
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<GraphRelation>(GraphRelation.class));
    }

    private static final String getGraphRelationBySourceConfigIdSql = "select * from graph_relation where source_config_id = ? or name = ?";
    public GraphRelation getGraphRelationByIdOrName(Integer sourceConfigId, String name) {
        List<GraphRelation> graphRelationList = jdbcTemplate.query(getGraphRelationBySourceConfigIdSql, new Object[]{sourceConfigId, name},
                new BeanPropertyRowMapper<GraphRelation>(GraphRelation.class));
        return graphRelationList.size() == 0 ? null : graphRelationList.get(0);
    }

    private static final String getGraphRelationBySourceConfigIdFilterIdSql = "select * from graph_relation where (source_config_id = ? or name = ?) and id != ?";
    public GraphRelation getGraphRelationByIdOrNameFilterId(Integer sourceConfigId, String name, Integer newId) {
        List<GraphRelation> graphRelationList = jdbcTemplate.query(getGraphRelationBySourceConfigIdFilterIdSql, new Object[]{sourceConfigId, name, newId},
                new BeanPropertyRowMapper<GraphRelation>(GraphRelation.class));
        return graphRelationList.size() == 0 ? null : graphRelationList.get(0);
    }

    private static final String deleteRelationSql = "delete from graph_relation where id = ?";
    public int deleteRelation(int id) {
        return jdbcTemplate.update(deleteRelationSql, id);
    }

    private static final String getGraphConfigSql = "select a.* from graph_relation a join datasource_config b on a.source_config_id = b.id" +
            " where b.target_table = ?";
    public List<GraphRelation> getGraphConfig(String targetTableName) {
        return jdbcTemplate.query(getGraphConfigSql, new Object[]{targetTableName}, new BeanPropertyRowMapper<GraphRelation>(GraphRelation.class));
    }

    public GraphResult queryGraphDetail(SearchGraphParam searchGraphParam) {
        final String queryGraphDetailAql = String.format("for d in %s filter d._from == @from AND d._to == @to ", searchGraphParam.getTable());
        StringBuilder sb = new StringBuilder(queryGraphDetailAql);
        sb.append(buildFilterAttr(searchGraphParam.getFilters()));
        sb.append(buildPage(searchGraphParam.getOffset(), searchGraphParam.getLimit()));
        sb.append(" RETURN d");
        log.info("Query AQL : {}", sb.toString());
        Map<String, Object> bind = Maps.newHashMap();
        bind.put("from", searchGraphParam.getFrom());
        bind.put("to", searchGraphParam.getTo());
        AqlQueryOptions aqlQueryOptions = null;
        if (searchGraphParam.getNeedCount() != null && searchGraphParam.getNeedCount()) {
            aqlQueryOptions = new AqlQueryOptions();
            aqlQueryOptions.fullCount(true);
        }
        ArangoCursor<Map> ret = arangoDatabase.query(sb.toString(), bind, aqlQueryOptions, Map.class);
        GraphResult graphResult = new GraphResult();
        graphResult.setCount(ret.getStats().getFullCount());
        graphResult.setList(ret.asListRemaining());
        return graphResult;
    }

    public List<Map> queryForDemo(String graphName) {
        final String aql = String.format("for d in %s limit 1 return d", graphName);
        return arangoDatabase.query(aql, null, null, Map.class).asListRemaining();
    }

    private String buildPage(int offset, int limit) {
        return " LIMIT " + offset + ", " + limit;
    }

    private String buildFilterAttr(Map<String, Object> filters) {
        if (filters == null || filters.size() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(" filter 1 == 1");
        for (Map.Entry<String, Object> one : filters.entrySet()) {
            String key = one.getKey();
            Object value = one.getValue();
            if (!(value instanceof Map)) {
                continue;
            }
            Map<String, Object> attrs = (Map<String, Object>) value;
            for (Map.Entry<String, Object> attr : attrs.entrySet()) {
                String name = attr.getKey();
                Object v = attr.getValue();
                if ("min".equals(name)) {
                    sb.append(buildFilterCondition(key, v, ">="));
                } else if ("max".equals(name)) {
                    sb.append(buildFilterCondition(key, v, "<="));
                } else if ("eq".equals(name)) {
                    sb.append(buildFilterCondition(key, v, "=="));
                }
            }
        }
        return sb.toString();
    }

    public String buildFilterCondition(String key, Object v, String op) {
        String value = null;
        if (v instanceof Integer || v instanceof Long || v instanceof Double || v instanceof Float) {
            value = v.toString();
        } else if (v instanceof String) {
            value = "'" + ((String) v).replaceAll("'", "") + "'";
        }
        if (StringUtils.isNoneEmpty(value)) {
            return " AND d." + key + " " + op + " " + value;
        }
        return "";
    }

    public Map<String,Object> getDSConfigTableNameByGRelation(Integer grelationID) {
        String sql = "select (select target_table from datasource_config  where id = (" +
                "select source_config_id from graph_relation where id = ?)" +
                ") tableName , (select name from graph_relation where id = ?) label ";
        Map<String,Object> tableNameAndLabel = null;
        List args = new ArrayList();
        args.add(grelationID);
        args.add(grelationID);
        try {
            tableNameAndLabel = this.jdbcTemplate.queryForMap(sql,args.toArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tableNameAndLabel;
    }

    public Map getRandomSingleDoc(String arangoTableName) {
        String aql = "for doc in @@collection\n" +
                     "limit 1\n" +
                     "return doc";
        Map bindVars = new HashMap();
        bindVars.put("@collection",arangoTableName);
        ArangoCursor<Map> cursor = this.arangoDatabase.query(aql,bindVars,null,Map.class);

        return getAndClose(cursor).get(0);
    }

    public List<Map> getRandomSinglePath(String arangoTableName, String fromID) {
        String aql = "WITH Company,Person\n" +
                    "FOR v,e,p IN 1..1 outbound @fromID @@collection\n" +
                    "limit 1\n" +
                    "return p";
        Map bindVars = new HashMap();
        bindVars.put("@collection",arangoTableName);
        bindVars.put("fromID",fromID);
        ArangoCursor<Map> cursor = this.arangoDatabase.query(aql,bindVars,null,Map.class);

        return getAndClose(cursor);
    }

    private <T> List<T> getAndClose(ArangoCursor<T> cursor) {
        if (cursor != null) {
            try {
                return cursor.asListRemaining();
            } finally {
                if (cursor != null) {
                    try {
                        cursor.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

}
