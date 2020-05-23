package com.haizhi.iap.mobile.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haizhi.iap.common.Wrapper;
import com.haizhi.iap.mobile.bean.FollowItem;
import com.haizhi.iap.mobile.bean.User;
import com.haizhi.iap.mobile.bean.normal.GraphEdge;
import com.haizhi.iap.mobile.bean.normal.MongoTermsQuery;
import com.haizhi.iap.mobile.bean.param.SearchParamWithDirection;
import com.haizhi.iap.mobile.bean.result.ClusterPath;
import com.haizhi.iap.mobile.bean.result.Graph2;
import com.haizhi.iap.mobile.conf.*;
import com.haizhi.iap.mobile.enums.ClusterDomainName;
import com.haizhi.iap.mobile.enums.ClusterDomainType;
import com.haizhi.iap.mobile.repo.BasicSqlRepo;
import com.haizhi.iap.mobile.repo.GraphRepo;
import com.haizhi.iap.mobile.repo.MongoRepo;
import com.haizhi.iap.mobile.repo.UserRepo;
import com.haizhi.iap.mobile.util.GraphEdgeLabelUtil;
import com.haizhi.iap.mobile.util.PersonMergeUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by thomas on 18/4/13.
 */
@Service
public class EnterpriseService
{
    @Autowired
    private MongoRepo mongoRepo;

    @Autowired
    private GraphRepo graphRepo;

    @Autowired
    private GraphService graphService;

    @Autowired
    private PersonMergeUtil personMergeUtil;

    @Autowired
    private BasicSqlRepo<FollowItem> followItemRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private GraphEdgeLabelUtil graphEdgeLabelUtil;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private IapSearchRestConf iapSearchRestConf;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 公司所属的股票板块名
     *
     * @return
     */
    public Map<String, Object> addStockSectorName(Map<String, Object> company)
    {
        if(!CollectionUtils.isEmpty(company))
        {
            String stockCode = Optional.ofNullable(company.get(MongoSchemaConstants.FIELD_STOCK_CODE)).map(Object::toString).orElse(null);
            String stockSectorName = "";
            if (StringUtils.isNotBlank(stockCode))
            {
                stockSectorName = PublicSectorConf.getSectorConfMap().entrySet().stream().filter(entry -> stockCode.startsWith(entry.getKey())).map(Map.Entry::getValue).reduce((x, y) -> x + ", " + y).orElse("");
            }
            company.put("stock", stockSectorName);
        }
        return company;
    }

    /**
     * 公司简介
     *
     * @param username
     * @param companyNames
     * @return
     */
    public List<Map<String, Object>> brief(String username, Collection<String> companyNames)
    {
        List<Map<String, Object>> companyInfos = getBasicInfo(companyNames);
        if(!CollectionUtils.isEmpty(companyInfos))
        {
            Map<String, Map<String, Object>> followAndMonitorStatusMap = followAndMonitorStatus(username, companyNames);
            //actual controller
            Map<String, List<Map<String, Object>>> actualControllerMap = new HashMap<>();
            companyNames.forEach(companyName -> {
                List<Map<String, Object>> actualControllers = graphRepo.getActualControlMan(companyName);
                actualControllerMap.put(companyName, actualControllers);
            });
            companyInfos.forEach(companyInfo -> {
                Map<String, Object> followAndMonitorStatus = followAndMonitorStatusMap.getOrDefault(companyInfo.get(MongoSchemaConstants.FIELD_COMPANY).toString(), Collections.emptyMap());
                companyInfo.put("is_follow", followAndMonitorStatus.remove("is_follow"));
                companyInfo.put("monitor_status", followAndMonitorStatus);
                List<Map<String, Object>> actualControllers = actualControllerMap.getOrDefault(companyInfo.get(MongoSchemaConstants.FIELD_COMPANY).toString(), Collections.emptyList());
                companyInfo.put("actualControllers", actualControllers);
            });
        }
        return companyInfos;
    }

    /**
     * 工商信息(mongo中查数据)
     *
     * @param companyNames
     * @return
     */
    public List<Map<String, Object>> getBasicInfo(Collection<String> companyNames)
    {
        if(CollectionUtils.isEmpty(companyNames)) return Collections.emptyList();
        MongoTermsQuery termsQuery = MongoTermsQuery.builder().table(MongoSchemaConstants.TABLE_ENTERPRISE_DATA_GOV).query(Pair.of("company", companyNames)).build();
        return mongoRepo.query(termsQuery).stream().filter(res -> !CollectionUtils.isEmpty(res)).map(this::addStockSectorName).collect(Collectors.toList());
    }

    /**
     * 获取公司的关注、监控状态
     *
     * @param username
     * @param companyNames
     * @return
     */
    public Map<String, Map<String, Object>> followAndMonitorStatus(String username, Collection<String> companyNames)
    {
        Map<String, Map<String, Object>> followAndMonitorStatusMap = new HashMap<>();
        if(!CollectionUtils.isEmpty(companyNames))
        {
            User user = userRepo.findOneByName(username);
            String sql = "SELECT * FROM " + SqlSchemaConstants.TABLE_FOLLOW_ITEM + " WHERE user_id = :userId AND company_name in (:companys)";
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("userId", user.getId());
            paramMap.put("companys", companyNames);
            List<FollowItem> followItems = followItemRepo.findAll(sql, paramMap);
            for (FollowItem followItem : followItems)
            {
                Map<String, Object> map = new HashMap<>();
                map.put("is_follow", followItem.getIsFollow());
                map.put("marketing_notify", followItem.getMarketingNotify());
                map.put("closely_marketing_notify", followItem.getCloselyMarketingNotify());
                map.put("risk_notify", followItem.getRiskNotify());
                map.put("closely_risk_notify", followItem.getCloselyRiskNotify());
                followAndMonitorStatusMap.put(followItem.getCompanyName(), map);
            }
        }
        return followAndMonitorStatusMap;
    }

    /**
     * 股东信息，展开1层。对于上市企业，查找tradable_share关系。对于非上市企业，查找invest关系
     *
     * @param companyName
     * @return
     */
    public Pair<Graph2, Long> getShareHolder(String companyName, Integer offset, Integer size)
    {
        Pair<Graph2, Long> pair = searchRelation(companyName, offset, size, startVertex -> {
            //search tradable_share first
            List<Map<String, Map<String, Object>>> results = graphRepo.traversalGraphWithParams(
                    StringUtils.join(ArangoEdgeConf.TRADABLE_SHARE.getVertexCollection(), ", "),
                    Collections.singleton(ArangoEdgeConf.TRADABLE_SHARE.getTableName()), 1, Collections.singleton(startVertex),
                    GraphEdge.Direction.IN, null, null, null, null
            );
            //no tradable_share data, that means this company isn't a listed company, try to search invest
            if (CollectionUtils.isEmpty(results))
            {
                results = graphRepo.traversalGraphWithParams(
                        StringUtils.join(ArangoEdgeConf.INVEST.getVertexCollection(), ", "),
                        Collections.singleton(ArangoEdgeConf.INVEST.getTableName()), 1, Collections.singleton(startVertex),
                        GraphEdge.Direction.IN, null, null, null, null
                );
            }
            //this piece of code is kinda ugly.....
            else {
                for (Map<String, Map<String, Object>> map : results)
                {
                    Map<String, Object> edge = map.get("edges");
                    Map<String, Object> fromVertex = map.get("vertexes");
                    String fromVertexType = StringUtils.substringBefore(fromVertex.get("_id").toString(), "/");
                    if(ArangoSchemaConstants.TABLE_PERSON.equals(fromVertexType))
                        edge.put("shareholder_type", "自然人股东");
                    else if(ArangoSchemaConstants.TABLE_COMPANY.equals(fromVertexType))
                        edge.put("shareholder_type", "企业法人");
                }
            }
            return results;
        });

        Map<String, Map<String, Object>> idVertexMap = new HashMap<>();
        if(pair != null && pair.getLeft() != null && !CollectionUtils.isEmpty(pair.getLeft().getVertexes()))
        {
            pair.getLeft().getVertexes().stream().filter(vertex -> !CollectionUtils.isEmpty(vertex)).forEach(vertex -> idVertexMap.put(vertex.get("_id").toString(), vertex));
            if(!CollectionUtils.isEmpty(pair.getLeft().getEdges()) && !CollectionUtils.isEmpty(idVertexMap))
                pair.getLeft().getEdges().forEach(edge -> {
                    Double percentage = graphEdgeLabelUtil.getInvestPercentage(edge, idVertexMap);
                    if(percentage != null) edge.put("percentage", String.format("%s%%", GraphEdgeLabelUtil.EdgeLabelMaker.format(percentage)));
                });
        }
        return pair;
    }

    /**
     * 高管信息，展开1层
     *
     * @param companyName
     * @return
     */
    public Pair<Graph2, Long> getOfficer(String companyName, Integer offset, Integer size)
    {
        return searchRelation(companyName, offset, size, startVertex -> personMergeUtil.expandWithPersonMerge(
                StringUtils.join(ArangoEdgeConf.OFFICER.getVertexCollection(), ", "),
                Collections.singleton(ArangoEdgeConf.OFFICER.getTableName()), 1, Collections.singleton(startVertex),
                GraphEdge.Direction.IN, null, null, null, null
        ));
    }

    /**
     * 通用的关系查找接口，以某个企业为中心点，加入一些前置、后置操作
     *
     * @param companyName
     * @param offset
     * @param size
     * @param function 入参为startVertexId，返回arangodb图数据
     * @return
     */
    public Pair<Graph2, Long> searchRelation(String companyName, Integer offset, Integer size, Function<String, List<Map<String, Map<String, Object>>>> function)
    {
        Map<String, Object> company = graphRepo.getCompanyByName(companyName);
        if(CollectionUtils.isEmpty(company)) return Pair.of(new Graph2(), 0L);

        String startVertex = company.get("_id").toString();
        //search relation
        List<Map<String, Map<String, Object>>> results = function.apply(startVertex);
        Long cnt = Optional.ofNullable(results).map(List::size).map(v -> (long) v).orElse(0L);
        results = graphService.limit(results, offset, size);

        Graph2 graph = graphService.buildGraphFromTraverseResult(results, company);
        //自然人融合
        graph = personMergeUtil.mergePerson(graph);
        return Pair.of(graph, cnt);
    }

    /**
     * 对外投资信息，展开1层
     *
     * @param companyName
     * @return
     */
    public Pair<Graph2, Long> getInvest(String companyName, Integer offset, Integer size)
    {
        Pair<Graph2, Long> pair = searchRelation(companyName, offset, size, startVertex -> {
            List<Map<String, Map<String, Object>>> results = personMergeUtil.expandWithPersonMerge(
                    StringUtils.join(ArangoEdgeConf.INVEST.getVertexCollection(), ", "),
                    Collections.singleton(ArangoEdgeConf.INVEST.getTableName()), 1, Collections.singleton(startVertex),
                    GraphEdge.Direction.OUT, null, null, null, null
            );
            for (Map<String, Map<String, Object>> result : results)
            {
                Map<String, Object> vertex = result.get("vertexes");
                if(!CollectionUtils.isEmpty(vertex))
                {
                    Map<String, Object> basicInfo = getBasicInfo(Collections.singleton(vertex.get("name").toString())).stream().findFirst().orElse(Collections.emptyMap());
                    basicInfo.remove("_id");
                    vertex.putAll(basicInfo);
                }
            }
            return results;
        });

        Map<String, Map<String, Object>> idVertexMap = new HashMap<>();
        if(pair != null && pair.getLeft() != null && !CollectionUtils.isEmpty(pair.getLeft().getVertexes()))
        {
            pair.getLeft().getVertexes().stream().filter(vertex -> !CollectionUtils.isEmpty(vertex)).forEach(vertex -> idVertexMap.put(vertex.get("_id").toString(), vertex));
            if(!CollectionUtils.isEmpty(pair.getLeft().getEdges()) && !CollectionUtils.isEmpty(idVertexMap))
                pair.getLeft().getEdges().forEach(edge -> {
                    Double percentage = graphEdgeLabelUtil.getInvestPercentage(edge, idVertexMap);
                    if(percentage != null) edge.put("percentage", String.format("%s%%", GraphEdgeLabelUtil.EdgeLabelMaker.format(percentage)));
                });
        }
        return pair;
    }

    /**
     * 资金往来信息，展开1层
     *
     * @param companyName
     * @return
     */
    public Pair<Graph2, Long> getMoneyFlow(String companyName, Integer offset, Integer size, GraphEdge.Direction direction)
    {
        return searchRelation(companyName, offset, size, startVertex -> {
            return personMergeUtil.expandWithPersonMerge(
                    StringUtils.join(ArangoEdgeConf.MONEY_FLOW.getVertexCollection(), ", "),
                    Collections.singleton(ArangoEdgeConf.MONEY_FLOW.getTableName()), 1, Collections.singleton(startVertex),
                    direction, null, null, null, null
            );
        });
    }

    /**
     * 一致行动人，展开1层
     *
     * @param companyName
     * @return
     */
    public Pair<Graph2, Long> getConcert(String companyName, Integer offset, Integer size)
    {
        return searchRelation(companyName, offset, size, startVertex -> {
            List<Map<String, Map<String, Object>>> results = personMergeUtil.expandWithPersonMerge(
                    StringUtils.join(ArangoEdgeConf.CONCERT.getVertexCollection(), ", "),
                    Collections.singleton(ArangoEdgeConf.CONCERT.getTableName()), 1, Collections.singleton(startVertex),
                    GraphEdge.Direction.ALL, null, null, null, null
            );
            //找出一致行动对象
            for (Map<String, Map<String, Object>> result : results)
            {
                Map<String, Object> edge = result.get("edges");
                String target = edge.getOrDefault("target", "").toString();
                if(StringUtils.isNotBlank(target) && !"all".equalsIgnoreCase(target))
                {
                    Map<String, Object> targetMap = graphRepo.fetchDocument(target);
                    edge.put("target", targetMap);
                }
            }
            return results;
        });
    }

    /**
     * 担保信息，展开1层
     *
     * @param companyName
     * @return
     */
    public Pair<Graph2, Long> getGuarantee(String companyName, Integer offset, Integer size, GraphEdge.Direction direction)
    {
        return searchRelation(companyName, offset, size, startVertex -> {
            return personMergeUtil.expandWithPersonMerge(
                    StringUtils.join(ArangoEdgeConf.GUARANTEE.getVertexCollection(), ", "),
                    Collections.singleton(ArangoEdgeConf.GUARANTEE.getTableName()), 1, Collections.singleton(startVertex),
                    direction, null, null, null, null
            );
        });
    }

    /**
     * 招中标信息，展开1层
     *
     * @param companyName
     * @return
     */
    public Pair<Graph2, Long> getBidInfo(String companyName, Integer offset, Integer size, GraphEdge.Direction direction)
    {
        return searchRelation(companyName, offset, size, startVertex -> {
            return personMergeUtil.expandWithPersonMerge(
                    StringUtils.join(ArangoEdgeConf.PARTY_BID.getVertexCollection(), ", "),
                    Collections.singleton(ArangoEdgeConf.PARTY_BID.getTableName()), 1, Collections.singleton(startVertex),
                    direction, null, null, null, null
            );
        });
    }

    /**
     * 上下游企业
     *
     * @param searchParam
     * @return
     */
    public Pair<Graph2, Long> getUpDownStream(SearchParamWithDirection searchParam)
    {
        Map<String, Object> company = graphRepo.getCompanyByName(searchParam.getKeyword());
        if(CollectionUtils.isEmpty(company)) return Pair.of(new Graph2(), 0L);

        String id = company.get("_id").toString();
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("id", id);
        requestParams.put("domainType", ClusterDomainType.UPSTREAM.name().toLowerCase());
        requestParams.put("domainName", ClusterDomainName.CHANGSHA.name().toLowerCase());
        Wrapper wrapper = restTemplate.getForObject(iapSearchRestConf.getGraphCidUrl() + "?entity_id={id}&domain_name={domainName}&type={domainType}", Wrapper.class, requestParams);
        if(wrapper.getStatus() != 0) return Pair.of(new Graph2(), 0L);

        String clusterId = (String) wrapper.getData();
        requestParams.put("id", clusterId);
        wrapper = restTemplate.getForObject(iapSearchRestConf.getGraphClusterUrl() + "?cid={id}&domain_name={domainName}&type={domainType}&result_type=graph", Wrapper.class, requestParams);
        if(wrapper.getStatus() != 0) return Pair.of(new Graph2(), 0L);

        Graph2 graph = null;
        try {
            graph = objectMapper.readValue(objectMapper.writeValueAsString(wrapper.getData()), Graph2.class);
        } catch (IOException ignore) {}
        if(graph == null) return Pair.of(new Graph2(), 0L);
        graph = graphService.reachableGraph(graph, company, searchParam.getDirection());
        if(!CollectionUtils.isEmpty(graph.getVertexes()))
        {
            graph.getVertexes().forEach(vertex -> {
                Map<String, Object> basicInfo = getBasicInfo(Collections.singleton(vertex.get("name").toString())).stream().findFirst().orElse(Collections.emptyMap());
                basicInfo.remove("_id");
                vertex.putAll(basicInfo);
            });
        }
        return Pair.of(graph, Optional.of(graph).map(Graph2::getEdges).map(list -> (long) list.size()).orElse(0L));
    }

    /**
     * 关联担保
     *
     * @param searchParam
     * @return
     */
    public Pair<Graph2, Long> getGuarantee(SearchParamWithDirection searchParam)
    {
        Map<String, Object> company = graphRepo.getCompanyByName(searchParam.getKeyword());
        if(CollectionUtils.isEmpty(company)) return Pair.of(new Graph2(), 0L);

        String id = company.get("_id").toString();
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("id", id);
        requestParams.put("domainType", ClusterDomainType.DANBAO.name().toLowerCase());
        requestParams.put("domainName", ClusterDomainName.CHANGSHA.name().toLowerCase());

        Wrapper wrapper = restTemplate.getForObject(iapSearchRestConf.getGraphEntityPathUrl() + "?entity_id={id}&domain_name={domainName}&type={domainType}", Wrapper.class, requestParams);
        if(wrapper.getStatus() != 0) return Pair.of(new Graph2(), 0L);

        List<ClusterPath> clusterPaths = null;
        try {
            clusterPaths = objectMapper.readValue(objectMapper.writeValueAsString(wrapper.getData()), new TypeReference<List<ClusterPath>>(){});
        } catch (IOException ignore) {}
        if(CollectionUtils.isEmpty(clusterPaths)) return Pair.of(new Graph2(), 0L);

        try {
            List<Map<String, Object>> vertices = new ArrayList<>();
            List<Map<String, Object>> edges = new ArrayList<>();
            for (ClusterPath clusterPath : clusterPaths)
            {
                List<Map<String, Object>> _edges = objectMapper.readValue(clusterPath.getPaths(), new TypeReference<List<Map<String, Object>>>() {});
                List<Map<String, Object>> _vertices = clusterPath.getVertexes();
                if(!CollectionUtils.isEmpty(_vertices)) vertices.addAll(_vertices);
                if(!CollectionUtils.isEmpty(_edges)) edges.addAll(_edges);
            }
            Graph2 graph = new Graph2(vertices, edges);
            graph = graphService.reachableGraph(graph, company, searchParam.getDirection());
            return Pair.of(graph, Optional.of(graph).map(Graph2::getEdges).map(list -> (long) list.size()).orElse(0L));
        } catch (IOException ignore) {}
        return Pair.of(new Graph2(), 0L);
    }
}
