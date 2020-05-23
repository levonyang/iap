package com.haizhi.iap.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.haizhi.iap.search.controller.model.DataItem;
import com.haizhi.iap.search.controller.model.Graph;
import com.haizhi.iap.search.model.CompanyGroup;
import com.haizhi.iap.search.repo.CompanyGroupRepo;
import com.haizhi.iap.search.repo.GroupDetailRepo;
import com.haizhi.iap.search.repo.RedisRepo;
import com.haizhi.iap.search.service.GraphGroupService;
import com.haizhi.iap.search.service.GraphService;
import com.haizhi.iap.search.service.GroupService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
* @description 族谱接口实现
* @author LewisLouis
* @date 2018/8/20
*/
@Slf4j
@Service
public class GraphGroupsServiceImpl implements GraphGroupService {

    /**
     * @description 获取指定族谱的边
     * @param type 族谱类型
     *（如profile_enterprise_info 关联集团
     * market_updown_info  上下游
     * risk_propagation  风险传导
     * risk_guarantee_info 关联担保
     * risk_black_info  黑名单）
    * @param subType 族谱子类型（如circle）
    * @param offset 开始行数
    * @param count 返回数量
     * @return DataItem
     * @author LewisLouis
     * @date 2018/8/20
     */
    @Override
    public DataItem findGroupsByTypeWithOutPaths(String type, String subType, Integer offset, Integer count){

        String realSubType = (subType.equals("_all") || Strings.isNullOrEmpty(subType))? null: subType;
        List<CompanyGroup> groups = companyGroupRepo.findGroupsByTypeWithOutPaths(type,realSubType,offset,count);
        Long groupsCount = companyGroupRepo.findGroupsCountByType(type,realSubType);

        DataItem dataItem = new DataItem();
        dataItem.setData(groups);
        dataItem.setTotalCount(groupsCount);

        return dataItem;

    }

    /**
     * @description 获取指定族谱的簇子图信息
     * @param groupName 族谱名称
     * @param type 族谱类型
     * @return com.haizhi.iap.search.controller.model.Graph
     * @author LewisLouis
     * @date 2018/8/20
     */
    @Override
    public Graph findOneGroupGraph(String groupName, String type){

        Graph resGraph;

        //首先尝试从缓存中获取该族谱信息
        String cacheGraph = redisRepo.getGraphGroup(groupName, type);


        if (!Strings.isNullOrEmpty(cacheGraph)) {
            try {
                resGraph = objectMapper.readValue(cacheGraph, Graph.class);
                if (resGraph.getVertexes() != null
                        && (!resGraph.getVertexes().isEmpty())) {
                    return resGraph;
                }
            } catch (Exception e) {
                log.warn(String.format("Get one group:%s from cache failed! %s",groupName, e));
            }
        }

        CompanyGroup group = companyGroupRepo.findOneGroup(groupName,type);
        if (null == group){
            log.error(String.format("Get one group:%s from db failed!",groupName));
            return null;
        }

        Pair<List<Map<String, Object>>, List<Map<String, Object>>> pair = processPaths(group.getPaths());

        //初始化边列表
        List<Map<String, Object>> edges = Collections.EMPTY_LIST;

        if (!Strings.isNullOrEmpty(group.getPaths())) {
            try {
                edges = objectMapper.readValue(group.getPaths(), List.class);
            } catch (Exception e) {
                log.warn("{}", e);
            }
        }

        //完善顶点信息(将Monogo中的公司详情信息融合到对应的顶点信息中)
        groupService.completeVertices(pair.getLeft(),false);

        resGraph = graphService.generateByEdgesNoArango(edges, pair.getLeft());

        try {
            redisRepo.setGraphGroup(groupName,type,objectMapper.writeValueAsString(resGraph));
        } catch (Exception e) {
            log.warn("{}", e);
        }
        return resGraph;

    }



    /**
     * @param groupName 族谱名称
     * @param type 族谱类型
     * @return com.haizhi.iap.search.model.CompanyGroup
     * @description 获取指定族谱信息
     * @author LewisLouis
     * @date 2018/8/20
     */
    @Override
    public CompanyGroup findOneGroup(String groupName, String type) {

        CompanyGroup group = companyGroupRepo.findOneGroup(groupName,type);
        if (null == group){
            log.error(String.format("Get one group:%s from db failed!",groupName));
            return null;
        }

        if (Strings.isNullOrEmpty(group.getPaths())){
            return group;
        }

        //处理族谱信息的边和顶点
        group = processCompanyGroup(group);

        return group;
    }

    /**
     * @param entityId  实体Id
     * @param type 族谱类型
     * @return 族谱名称
     * @description 根据实体Id获取所属的族谱名称
     * @author LewisLouis
     * @date 2018/8/20
     */
    @Override
    public String findGroupNameByEntity(String entityId, String type) {
        List<String> groupNames = groupDetailRepo.findGroupNameByEntity(entityId,type);
        if ((null != groupNames) && (!groupNames.isEmpty())){
            return groupNames.get(0);
        }

        return "";
    }

    /**
     * @param type 族谱类型
     * @return java.util.Map<java.lang.String , java.lang.Long> <变类型名称，对应的族谱数量>
     * @description 统计指定族谱类型的边类型数量
     * @author LewisLouis
     * @date 2018/8/20
     */
    @Override
    public Map<String, Long> findSubTypes(String type) {
        return companyGroupRepo.findSubTypes(type);
    }

    /**
    * @description 根据族谱类型获取边列表
    * @param type 族谱类型
    * @param subType 族谱边类型
    * @param offset
    * @param count
    * @return com.haizhi.iap.search.controller.model.DataItem
    * @author LewisLouis
    * @date 2018/8/20
    */
    @Override
    public DataItem findGroupPaths(String type, String subType, Integer offset, Integer count) {

        String realSubType = (type.equals("_all") || Strings.isNullOrEmpty(type))? null: subType;
        List<CompanyGroup> groups = companyGroupRepo.findGroupsByType(type,realSubType,offset,count);
        groups.forEach(this::processCompanyGroup);

        Long groupsCount = companyGroupRepo.findGroupsCountByType(type,realSubType);

        DataItem dataItem = new DataItem();
        dataItem.setData(groups);
        dataItem.setTotalCount(groupsCount);

        return dataItem;
    }

    /**
     * @param type 族谱类型
     * @param entityId  实体Id
     * @param entityId
     * @return java.util.List<com.haizhi.iap.search.model.CompanyGroup>
     * @description 获取实体相关的边信息
     * @author LewisLouis
     * @date 2018/8/20
     */
    @Override
    public List<CompanyGroup> findEntityPaths(String type, String entityId) {

        List<CompanyGroup> groups = new ArrayList<>();

        List<String> groupNames = groupDetailRepo.findGroupNameByEntity(entityId,type);

        if ((null == groupNames) || (groupNames.isEmpty())){
            return groups;
        }

        for (String groupName:groupNames) {
            CompanyGroup companyGroup = companyGroupRepo.findOneGroup(groupName,type);
            companyGroup = processCompanyGroup(companyGroup);
            groups.add(companyGroup);
        }

        return groups;
    }

    /**
    * @description 将数据库中（由数据文件导入）的Paths数据分为顶点集合result.getLeft()和边集合JSON.toJSONString(result.getRight())
    * @param paths 单个族谱的Paths信息
    * @return org.apache.commons.lang3.tuple.Pair<java.util.List<java.util.Map<java.lang.String,java.lang.Object>>,java.util.List<java.util.Map<java.lang.String,java.lang.Object>>>
    * @author LewisLouis
    * @date 2018/8/20
    */
    public Pair<List<Map<String, Object>>, List<Map<String, Object>>> processPaths(String paths) {
        Set<Map<String, Object>> vertexes = Sets.newHashSet();
        List<Map<String, Object>> edges = Lists.newArrayList();
        try {
            edges = objectMapper.readValue(paths, List.class);
            List<String> isExistId = new ArrayList<>();
            for (Map<String, Object> edge : edges) {
                if(edge.get("_from") != null) {
                    if(!isExistId.contains(edge.get("_from"))) {
                        Map<String, Object> company = new LinkedHashMap<String, Object>();
                        for (String oneKey:edge.keySet()){
                            if (oneKey.startsWith("src_")) {
                                company.put(oneKey.substring(4), edge.get(oneKey));
                            }
                        }
                        company.put("_id", edge.getOrDefault("_from", ""));
                        edge.put("_from_id", company.get("_id"));
                        vertexes.add(company);
                        isExistId.add(edge.get("_from").toString());
                    }
                }
                if(edge.get("_to") != null) {
                    if(!isExistId.contains(edge.get("_to"))) {
                        Map<String, Object> company = new LinkedHashMap<String, Object>();
                        for (String oneKey:edge.keySet()){
                            if (oneKey.startsWith("dst_")) {
                                company.put(oneKey.substring(4), edge.get(oneKey));
                            }
                        }
                        company.put("_id", edge.getOrDefault("_to", ""));
                        edge.put("_to_id", company.get("_id"));
                        vertexes.add(company);
                        isExistId.add(edge.get("_to").toString());
                    }
                }
            }
        } catch (IOException ex) {
            log.error("{}", ex);
        }
        return Pair.of(Lists.newArrayList(vertexes), edges);
    }

    /**
    * @description 处理族谱信息的边和顶点
    * @param group 待处理的族谱信息
    * @return com.haizhi.iap.search.model.CompanyGroup 已处理的族谱信息
    * @author LewisLouis
    * @date 2018/8/20
    */
    private CompanyGroup processCompanyGroup(CompanyGroup group) {
        if (group == null || Strings.isNullOrEmpty(group.getPaths())) {
            return group;
        }
        Pair<List<Map<String, Object>>, List<Map<String, Object>>> pair = processPaths(group.getPaths());
        group.setVertexes(pair.getLeft());
        group.setPaths(JSON.toJSONString(pair.getRight()));
        return group;
    }

    @Setter
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    CompanyGroupRepo companyGroupRepo;

    @Autowired
    GroupDetailRepo groupDetailRepo;

    @Setter
    @Autowired
    RedisRepo redisRepo;

    @Setter
    @Autowired
    GraphService graphService;

    @Autowired
    private GroupService groupService;

}
