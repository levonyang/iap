package com.haizhi.iap.search.service;

import com.haizhi.iap.search.controller.model.*;
import com.haizhi.iap.search.model.GraphEdge;
import com.haizhi.iap.search.model.PersonFamiliars;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by chenbo on 17/2/23.
 */
public interface GraphService {

    Map<String, Object> brief(String company);

    Graph buildGraph(String id, GraphReq req, Boolean readCache);

    /**
     * @author thomas
     *
     * 根据Arangodb返回的路径数据，构建图数据结构（后续可能需要，勿删）
     * @param paths
     * @return
     */
    Graph buildGraph(List<Map<String, List<Map<String, Object>>>> paths);

    Map<String, Object> invest(String id, GraphReq req);

    PersonFamiliars getFamiliars(String id);

    Graph getFamiliar(String id, String target);

    Graph getRelationByName(String entityNameA, String entityNameB, GraphReq req);

    Graph checkGuaranteeCircle(String companyA, String companyB);

    Graph groupByCompany(String companyName);

    Graph groupById(String groupId);

    Tree queryOverviewRelation(String companyName);

    Graph groupByEntityId(String entityId);

    Graph getPersonMergeSuggested(String personA, String personB);

    List<Map<String, Object>> getPersonMergeSuggestedList(String personId);

    Map<String, Object> investOfficerAround(String key, int depth, int personMergeDepth, int limit);

    Map<String, Object> getCompanyOrPersonById(String id);

    Graph generateByEdges(List<Map<String, Object>> edges);

    Graph generateByEdgesNoArango(List edges, List vertexes);

    Map<String, Object> addLabel(Map<String, Object> edge);

    PageResult getArangoListByPage(ReqGuaranteeOrTransfer reqGuaranteeOrTransfer);

    Map<String,Object> getGroupList(GraphListReq req);

    Map<String,Object> getGroupDetail(Long GroupId);

    Map<String, List> getPathBySingleCollection(String collection, int depth, String id);

    List<String> getGroupList(String type,Integer offset, Integer count);

    Integer getTotalGroupCount(String type);

    GraphReq processReq(GraphReq req);

    Graph getRelationById(String companyIdA, String companyIdB, GraphReq req);

    /**
     * @author thomas
     * 股权结构展开
     *
     * @param name 顶点名字
     * @param depth 深度
     * @param direction 方向
     * @return
     */
    GraphVo stockRight(String name, GraphEdge.Direction direction, int depth);

    /**
     * @author thomas
     * 资金往来
     *
     * @param name 顶点名字
     * @param depth 深度
     * @param direction 方向
     * @return
     */
    GraphVo moneyFlow(String name, GraphEdge.Direction direction, int depth);

    /**
     * @author thomas
     * 集团派系
     *
     * @param name 顶点名字
     * @return
     */
    GraphVo companyGroup(String name);

    /**
     * @author thomas
     * 高管信息
     *
     * @param name 顶点名字
     * @param depth 深度
     * @param direction 方向
     * @return
     */
    GraphVo officer(String name, GraphEdge.Direction direction, int depth);

    /**
     * @author thomas
     * 投资族谱
     *
     * @param name 顶点名字
     * @param depth 深度
     * @param direction 方向
     * @return
     */
    GraphVo investGroup(String name, GraphEdge.Direction direction, int depth);

    /**
     * @author thomas
     * 关系概览
     *
     * @param name 顶点名字
     * @return
     */


    /**
     * @author thomas
     * 事件关联
     *
     * @param name 顶点名字
     * @param depth 深度
     * @return
     */
    GraphVo event(String name, int depth);

    /**
     * 生成受益人图谱
     * @param companyName
     * @return com.haizhi.iap.search.controller.model.Graph
     * @author caochao
     * @Date 2018/8/2
     */
    Graph generateAmlGraph(String companyName);

    /**
     * @param results
     * @param centralVertex
     * @return
     * @author thomas
     * 将traverse操作的结果转换成Graph2结构
     */
    default GraphVo buildGraphFromTraverseResult(List<Map<String, Map<String, Object>>> results, Map<String, Object> centralVertex)
    {
        if(CollectionUtils.isEmpty(results)) return null;
        List<Map<String, Object>> vertexes = results.stream().map(map -> map.get("vertexes")).filter(vertex -> !CollectionUtils.isEmpty(vertex)).distinct().collect(Collectors.toList());
        List<Map<String, Object>> edges = results.stream().map(map -> map.get("edges")).filter(edge -> !CollectionUtils.isEmpty(edge)).distinct().collect(Collectors.toList());
        if(!CollectionUtils.isEmpty(vertexes) && !CollectionUtils.isEmpty(centralVertex))
            vertexes.add(0, centralVertex);
        return new GraphVo(vertexes, edges);
    }
}
