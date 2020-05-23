package com.haizhi.iap.search.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.haizhi.iap.common.utils.CollectionUtil;
import com.haizhi.iap.search.constant.BelongInnerStatus;
import com.haizhi.iap.search.constant.CommonFields;
import com.haizhi.iap.search.constant.EntityType;
import com.haizhi.iap.search.constant.Fields;
import com.haizhi.iap.search.controller.model.GraphVo;
import com.haizhi.iap.search.enums.GraphField;
import com.haizhi.iap.search.model.qo.GroupMembersSearchQo;
import com.haizhi.iap.search.model.vo.GroupMembersVo;
import com.haizhi.iap.search.repo.CompanyGroupRepo;
import com.haizhi.iap.search.repo.EnterpriseRepo;
import com.haizhi.iap.search.repo.GroupDetailRepo;
import com.haizhi.iap.search.service.GroupService;
import com.haizhi.iap.search.utils.MonogoDocumentUtil;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
* @description 族谱信息操作接口实现
* @author liulu
* @date 2018/12/19
*/
@Service
@Slf4j
public class GroupServiceImpl implements GroupService {

    /**
     * @description 条件查询指定族谱的成员信息
     * @param groupMembersSearchQo
     * @return com.haizhi.iap.search.model.vo.GroupCompanyVo
     * @author liulu
     * @date 2018/12/19
     */
    @Override
    public GroupMembersVo findGroupMembers(GroupMembersSearchQo groupMembersSearchQo) {

        String paths = companyGroupRepo.findByGroupName(groupMembersSearchQo.getGroupName(),groupMembersSearchQo.getGroupType());

        GraphVo graphVo = buildGraphByPaths(paths);

        GroupMembersVo groupMembersVo = new GroupMembersVo();


        if ((null == graphVo) || CollectionUtils.isEmpty(graphVo.getVertexes())){
            return groupMembersVo;
        }

        List<Map<String,Object>> memberList = graphVo.getVertexes();

        this.filterGraphVertexes(memberList,groupMembersSearchQo);

        this.completeVertices(memberList,false);

        groupMembersVo.setMembers(memberList);

        return groupMembersVo;
    }

    /**
    * @description 过滤查询结果
    * @param vetexes
    * @param groupMembersSearchQo
    * @return void
    * @author liulu
    * @date 2018/12/25
    */
    private void filterGraphVertexes(List<Map<String,Object>> vetexes,GroupMembersSearchQo groupMembersSearchQo){

        if (CollectionUtils.isEmpty(vetexes)){
            return;
        }
        Iterator<Map<String,Object>> vertexIter = vetexes.iterator();
        while (vertexIter.hasNext()){
            Map<String,Object> oneEntityMap = vertexIter.next();

            String id = CollectionUtil.findMapValue(CommonFields._ID.getValue(),oneEntityMap);

            if (StringUtils.isEmpty(id)){
                vertexIter.remove();
                continue;
            }

            if (null != groupMembersSearchQo.getEntityType()){
                switch (groupMembersSearchQo.getEntityType()){
                    case COMPANY:
                        if (id.toLowerCase().startsWith(EntityType.PERSON.getValue().toLowerCase())){
                            vertexIter.remove();
                            continue;
                        }
                        break;
                    case PERSON:
                        if (id.toLowerCase().startsWith(EntityType.COMPANY.getValue().toLowerCase())){
                            vertexIter.remove();
                            continue;
                        }
                }
            }
            BelongInnerStatus belongInnerStatus = BelongInnerStatus.byValue(groupMembersSearchQo.getBelongInner());
            if ( null != belongInnerStatus){
                String belongInner = CollectionUtil.findMapValue(CommonFields.BELONG_INNER.getValue(),oneEntityMap);

                if (!StringUtils.isEmpty(belongInner)){
                    if (!belongInner.toLowerCase().equalsIgnoreCase(belongInnerStatus.getBoolValue())){
                        vertexIter.remove();
                        continue;
                    }
                }
            }

        }


    }


    /**
     * @description  完善公司顶点信息
     * @param vertices
    * @param needCompleteBelongInner 是否需要完善行内（即授信）、行外tag
     * @return void
     * @author liulu
     * @date 2018/12/25
     */
    @Override
    public void completeVertices(List<Map<String, Object>> vertices, Boolean needCompleteBelongInner){

        //查询monogo完善公司详情
        this.completeDetailVertices(vertices);

        //查询monogo完善上市公司信息
        this.completeListedVertices(vertices);

        //查询mysql完善行内（即授信）、行外tag
        if (needCompleteBelongInner){
            this.completeBelongInnerVertices(vertices);
        }
    }

    /**
     * @description 完善行内（授信信息）
     * @param vertices
     * @return void
     * @author liulu
     * @date 2018/12/25
     */
    private void completeBelongInnerVertices(List<Map<String,Object>> vertices){
        if (CollectionUtils.isEmpty(vertices)){
            return;
        }

        List<String> belongInnerEntitiesName = groupDetailRepo.findBelongInnderEntitiesName(BelongInnerStatus.Y.getValue());

        if (CollectionUtils.isEmpty(belongInnerEntitiesName)){
            return;
        }

        vertices.stream().forEach( vertexMap -> {
            String entityName = CollectionUtil.findMapValue(GraphField.NAME.getName(),vertexMap);

            Boolean isInner = belongInnerEntitiesName.contains(entityName);

            //设置企业行内(即授信)、行外tag
            vertexMap.put(CommonFields.BELONG_INNER.getValue(), isInner.toString());
        });
    }


    /**
     * @description 从顶点中解析出公司列表
     * @param vertices
     * @return java.util.Set<java.lang.String>
     * @author liulu
     * @date 2018/12/25
     */
    private Set<String> findCompanies(List<Map<String,Object>> vertices){
        if (CollectionUtils.isEmpty(vertices)){
            return new HashSet<>();
        }
        //从顶点中解析出公司列表
        Set<String> companyNames = new HashSet<>();
        vertices.stream().forEach(oneVertexMap -> {
            String companyName = CollectionUtil.findMapValue(GraphField.NAME.getName(),oneVertexMap);
            String id = CollectionUtil.findMapValue(GraphField._ID.getName(),oneVertexMap);
            if (StringUtils.isEmpty(companyName)
                    ||(!id.toLowerCase().startsWith(EntityType.COMPANY.getValue().toLowerCase()))){
                return;
            }
            companyNames.add(companyName);
        });
        return companyNames;
    }

    /**
     * @description 完善公司顶点的详情信息
     * @param vertices
     * @return void
     * @author liulu
     * @date 2018/12/24
     */
    private void completeDetailVertices(List<Map<String, Object>> vertices){
        if (CollectionUtils.isEmpty(vertices)){
            return;
        }
        //从顶点中解析出公司列表
        Set<String> companyNames = findCompanies(vertices);
        if (CollectionUtils.isEmpty(companyNames)){
            return;
        }

        //从monogo中查询查询所有公司的详情信息
        List<Map<String,Object>> companyDetails = findCompanyDetails(companyNames);
        if (CollectionUtils.isEmpty(companyDetails)){
            return;
        }

        //将monogo中的公司详情信息融合到顶点信息中
        companyNames.forEach(companyName -> {
            Map companyVertexMap = CollectionUtil.findMap(GraphField.NAME.getName(),companyName,vertices);

            Map companyDetailMap = CollectionUtil.findMap(CommonFields.COMPANY.getValue(),companyName,companyDetails);

            if (!CollectionUtils.isEmpty(companyVertexMap)
                    && (!CollectionUtils.isEmpty(companyDetailMap))){
                companyVertexMap.putAll(companyDetailMap);

                //设置企业经营状态
                String businessStatus = CollectionUtil.findMapValue(CommonFields.BUSINESS_STATUS.getValue(),companyDetailMap);
                companyVertexMap.put(CommonFields.IS_ABNORMAL_STATUS.getValue(), Fields.ABNORMAL_STATUS.contains(businessStatus));
            }
        });
    }

    /**
     * @description 完善顶点中的上市公司信息
     * @param
     * @return void
     * @author liulu
     * @date 2018/12/24
     */
    private void completeListedVertices(List<Map<String, Object>> vertices){
        if (CollectionUtils.isEmpty(vertices)){
            return;
        }
        //从顶点中解析出上市公司列表
        Set<String> listedCompanyNames = new HashSet<>();
        vertices.stream().forEach(oneVertexMap -> {
            String companyName = CollectionUtil.findMapValue(GraphField.NAME.getName(),oneVertexMap);
            String id = CollectionUtil.findMapValue(GraphField._ID.getName(),oneVertexMap);
            String listed = CollectionUtil.findMapValue(CommonFields.IS_LISTED.getValue(),oneVertexMap);
            if (StringUtils.isEmpty(companyName)
                    ||(!id.toLowerCase().startsWith(EntityType.COMPANY.getValue().toLowerCase()))
                    ||(!"true".equalsIgnoreCase(listed))){
                oneVertexMap.put(CommonFields.IS_LISTED.getValue(),"false");
                return;
            }
            oneVertexMap.put(CommonFields.IS_LISTED.getValue(),"true");
            listedCompanyNames.add(companyName);
        });


        //将monogo中的上市公司信息融合到顶点信息中
        listedCompanyNames.forEach(companyName -> {
            Map companyVertexMap = CollectionUtil.findMap(GraphField.NAME.getName(),companyName,vertices);

            //从mongo库中获取上市信息
            Document doc = enterpriseRepo.getListingInfoByCompany(companyName);
            Map<String,Object> listMapInfo =  MonogoDocumentUtil.documentToMap(doc);
            if (CollectionUtils.isEmpty(companyVertexMap) || CollectionUtils.isEmpty(listMapInfo)){
                return;
            }

            //融合上市板块信息
            String pubSector = doc.getString(CommonFields.PUBLIC_SECTOR.getValue());
            companyVertexMap.put(CommonFields.PUBLIC_SECTOR.getValue(), pubSector);

        });
    }

    /**
     * @description 根据公司列表从monogo中获取详情
     * @param companyNames
     * @return java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     * @author liulu
     * @date 2018/12/24
     */
    private List<Map<String,Object>> findCompanyDetails(Set<String> companyNames){
        List<Document> companyList = enterpriseRepo.getBasicOfCompanies(new ArrayList<>(companyNames));
        List<Map<String,Object>> companyDetails = MonogoDocumentUtil.documentToMap(companyList);
        return companyDetails;
    }

    /**
     * @description 将数据库中（由数据文件导入）的Paths数据解析为图查询的结果方式
     * @param paths 单个族谱的Paths信息
     * @return org.apache.commons.lang3.tuple.Pair<java.util.List<java.util.Map<java.lang.String,java.lang.Object>>,java.util.List<java.util.Map<java.lang.String,java.lang.Object>>>
     * @author LewisLouis
     * @date 2018/8/20
     */
    public GraphVo buildGraphByPaths(String paths) {
        if (StringUtils.isEmpty(paths)){
            return new GraphVo();
        }
        Set<Map<String, Object>> vertexes = Sets.newHashSet();
        List<Map<String, Object>> edges = Lists.newArrayList();
        try {
            edges = objectMapper.readValue(paths, List.class);
            List<String> isExistId = new ArrayList<>();
            for (Map<String, Object> edge : edges) {
                if(edge.get(CommonFields._FROM.getValue()) != null) {
                    if(!isExistId.contains(edge.get(CommonFields._FROM.getValue()))) {
                        Map<String, Object> company = new LinkedHashMap<String, Object>();
                        for (String oneKey:edge.keySet()){
                            if (oneKey.startsWith(CommonFields.SRC_.getValue())) {
                                company.put(oneKey.substring(4), edge.get(oneKey));
                            }
                        }
                        company.put(CommonFields._ID.getValue(), edge.getOrDefault(CommonFields._FROM.getValue(), ""));
                        edge.put(CommonFields._FROM_ID.getValue(), company.get(CommonFields._ID.getValue()));
                        vertexes.add(company);
                        isExistId.add(edge.get(CommonFields._FROM.getValue()).toString());
                    }
                }
                if(edge.get(CommonFields._TO.getValue()) != null) {
                    if(!isExistId.contains(edge.get(CommonFields._TO.getValue()))) {
                        Map<String, Object> company = new LinkedHashMap<String, Object>();
                        for (String oneKey:edge.keySet()){
                            if (oneKey.startsWith(CommonFields.DST_.getValue())) {
                                company.put(oneKey.substring(4), edge.get(oneKey));
                            }
                        }
                        company.put(CommonFields._ID.getValue(), edge.getOrDefault(CommonFields._TO.getValue(), ""));
                        edge.put(CommonFields._TO_ID.getValue(), company.get(CommonFields._ID.getValue()));
                        vertexes.add(company);
                        isExistId.add(edge.get(CommonFields._TO.getValue()).toString());
                    }
                }
            }
        } catch (IOException ex) {
            log.error("{}", ex);
        }
        GraphVo graphVo = new GraphVo();
        graphVo.setVertexes(Lists.newArrayList(vertexes));
        graphVo.setEdges(edges);

        return graphVo;
    }


    @Autowired
    private GroupDetailRepo groupDetailRepo;

    @Autowired
    private CompanyGroupRepo companyGroupRepo;

    @Autowired
    private EnterpriseRepo enterpriseRepo;

    @Autowired
    private ObjectMapper objectMapper;


}
