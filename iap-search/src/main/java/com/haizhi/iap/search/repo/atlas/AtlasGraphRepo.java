package com.haizhi.iap.search.repo.atlas;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.haizhi.iap.common.exception.ServiceAccessException;
import com.haizhi.iap.common.utils.MapBuilder;
import com.haizhi.iap.search.component.GraphWSHolder;
import com.haizhi.iap.search.controller.GraphFoxxWS;
import com.haizhi.iap.search.controller.GraphWS;
import com.haizhi.iap.search.controller.model.*;
import com.haizhi.iap.search.exception.SearchException;
import com.haizhi.iap.search.model.GraphEdge;
import com.haizhi.iap.search.service.impl.FoxxAdapterService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * Created by chenbo on 17/2/22.
 */
@Slf4j
@Repository
public class AtlasGraphRepo {

    @Autowired
    private GraphWSHolder graphWSHolder;

    @Value("${atlas.person.tablename:tv_person}")
    private String personTable;

    @Value("${atlas.company.tablename:tv_company}")
    private String companyTable;

    @Value("${atlas.person.namekey:entname}")
    private String personNameKey;

    @Value("${atlas.company.namekey:name}")
    private String companyNameKey;

    /**
     * @author thomas
     * traverse操作，返回路径信息（后续可能需要，勿删）
     *
     * @param withDocuments
     * @param tableName
     * @param depth
     * @param startVertices
     * @param endVertices
     * @param direction
     * @return
     */
    public List<Map<String, Object>> traversalGraphToGetPath(String withDocuments,String tableName, int depth, Set<String> startVertices,Set<String> endVertices,
                                                             GraphEdge.Direction direction) {
        String endArray = "'" + StringUtils.join(endVertices.toArray(), "','") + "'";
        String filterStr = String.format("filter LAST(p.vertices)._id in [%s] ",endArray);
        return atlasTraversalGraphToGetPath(withDocuments,tableName,depth,startVertices,direction,filterStr);
    }

    /**
     * 获取企业id
     * @param names
     * @return
     */
    public List<Map<String,Object>> getCompanyByNames(List<String> names) {
        String aql = "With "+companyTable+" FOR doc IN "+companyTable+" FILTER doc."+companyNameKey+" in [%s] return distinct {\"id\":doc._id} ";
        aql = String.format(aql,"'" + String.join("','",names) + "'");
        Map<String, Object> bindVars = new MapBuilder().build();
        List<Map<String, Object>> res = atlasExecuteAQL(new GraphQuery(aql, bindVars));
        return res;
    }

    /**
     * 获取人员id
     * @param names
     * @return
     */
    public List<Map<String,Object>> getPersonByNames(List<String> names) {
        String aql = "With "+personTable+" FOR doc IN "+personTable+" FILTER doc."+personNameKey+" in [%s] return distinct {\"id\":doc._id} ";
        aql = String.format(aql,"'" + String.join("','",names) + "'");
        Map<String, Object> bindVars = new MapBuilder().build();
        List<Map<String, Object>> res = atlasExecuteAQL(new GraphQuery(aql, bindVars));
        return res;
    }

    /**
     * 获取人员信息
     * @param ids
     * @return
     */
    public List<Map<String,Object>> getPersonByIds(List<String> ids) {
        String aql = "With "+personTable+" FOR doc IN "+personTable+" FILTER doc._id in [%s] return distinct doc ";
        aql = String.format(aql,"'" + String.join("','",ids) + "'");
        Map<String, Object> bindVars = new MapBuilder().build();
        List<Map<String, Object>> res = atlasExecuteAQL(new GraphQuery(aql, bindVars));
        return res;
    }

    /**
     * 获取企业信息
     * @param ids
     * @return
     */
    public List<Map<String,Object>> getCompanyByIds(List<String> ids) {
        String aql = "With "+companyTable+" FOR doc IN "+companyTable+" FILTER doc._id in [%s] return distinct doc ";
        aql = String.format(aql,"'" + String.join("','",ids) + "'");
        Map<String, Object> bindVars = new MapBuilder().build();
        List<Map<String, Object>> res = atlasExecuteAQL(new GraphQuery(aql, bindVars));
        return res;
    }


    /**
     * 执行aql获取结果
     * @param graphQuery
     * @return
     */
    private List<Map<String, Object>> atlasExecuteAQL(GraphQuery graphQuery) {
        try {
            GraphWS graphWS = graphWSHolder.getWs();
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
    public List<Map<String, Object>> atlasTraversalGraphToGetPath(String withDocuments,String tableName, int depth, Set<String> startVertices,
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

        return atlasExecuteAQL(new GraphQuery(aql, bindVars, 1000));
    }

    /**
     * 查询两个节点的最短路径
     * @param document
     * @param relationship
     * @param startid
     * @param endid
     * @param direction
     * @param filterStr
     * @return
     */
    public List<Map<String, Object>> shortPath(String document,
                                               String relationship,
                                               String startid,
                                               String endid,
                                               GraphEdge.Direction direction,
                                               String filterStr){
        StringBuilder sb = new StringBuilder();
        sb.append("with ").append(document);
        sb.append(" for v,e in ");
        sb.append(direction.getArangoMarker()).append(" shortest_path @start to @end ");
        sb.append(relationship);
        sb.append(" return {node:v,edge:e} ");
        Map<String, Object> bindVars = Maps.newHashMap();
        bindVars.put("start", startid);
        bindVars.put("end", endid);

        return atlasExecuteAQL(new GraphQuery(sb.toString(), bindVars, 1000));
    }

}
