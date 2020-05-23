package com.haizhi.iap.search.service;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.haizhi.iap.common.utils.CollectionUtil;
import com.haizhi.iap.search.constant.CommonFields;
import com.haizhi.iap.search.model.CompanyGroup;
import com.haizhi.iap.search.model.GroupDetail;
import com.haizhi.iap.search.repo.CompanyGroupRepo;
import com.haizhi.iap.search.repo.GroupDetailRepo;
import com.haizhi.iap.search.repo.RedisRepo;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 读取文件并将文件导成两张表
 * Created by yuding on 2018/8/16.
 */
@Slf4j
@Service
public class GroupsInsertProcessor implements Processor {

    @Setter
    @Autowired
    ObjectMapper objectMapper;

    @Setter
    @Autowired
    CompanyGroupRepo companyGroupRepo;

    @Setter
    @Autowired
    GroupDetailRepo groupDetailRepo;

    @Setter
    @Autowired
    RedisRepo redisRepo;

    @Value("${group.batch.insert.size:1000}")
    private Integer batchInsertSize;

    public void process(Exchange exchange) throws Exception {
        BufferedReader reader = null;
        String groupType = null;
        if (exchange.getIn().getBody() != null && exchange.getIn().getHeader("type") != null) {
            reader = (BufferedReader) exchange.getIn().getBody();
            //族谱信息类型,eg:risk_guarantee_info、risk_black_info、market_updown_info、risk_propagation等
            groupType = (String) exchange.getIn().getHeader("type");
        }
        log.info("start deleting company_group of type: {}", groupType);
        groupDetailRepo.delete(groupType);
        companyGroupRepo.delete(groupType);
        redisRepo.deleteGraphGroups(groupType);
        log.info("delete company_group of type: {}", groupType);

        String line;
        try {
            line = reader.readLine();
            //List<ClusterGroup> clusterList = Lists.newArrayList();
            //List<EntityClusterMap> mapList = Lists.newArrayList();
            List<CompanyGroup> groupList = Lists.newArrayList();
            List<GroupDetail> batchInsertGroupDetailList = Lists.newArrayList();
            Set<String> currentGroupEntityIds = new HashSet<>();
            Set<String> belongInnerEntityIds = new HashSet<>();
            Integer counter = 0;
            while (line != null) {
                Map<String, Object> data = objectMapper.readValue(line, Map.class);

                String groupName = CollectionUtil.findMapValue(CommonFields.GROUP_NAME.getValue(), data);

                currentGroupEntityIds.clear(); //当前族谱的实体Id列表
                belongInnerEntityIds.clear();  //当前族谱的行内实体Id列表
                //Integer pathCount = data.get("paths") == null || !(data.get("paths") instanceof List) ? 0 : ((List)data.get("paths")).size();
                String pathInfos = data.get(CommonFields.GROUP_PATHS.getValue()) == null ? "" : JSON.toJSONString(data.get(CommonFields.GROUP_PATHS.getValue()));
                String subType = CollectionUtil.findMapValue(CommonFields.GROUP_SUB_TYPE.getValue(), data);
                //插入映射
                if (data.get(CommonFields.GROUP_PATHS.getValue()) != null && data.get(CommonFields.GROUP_PATHS.getValue()) instanceof List) {
                    List<Map<String, Object>> paths = (List<Map<String, Object>>) data.get(CommonFields.GROUP_PATHS.getValue());

                    CompanyGroup companyGroup = new CompanyGroup();
                    companyGroup.setBelongInner(false);

                    for (Map<String, Object> path : paths) {
                        if (path.get(CommonFields._FROM.getValue()) != null) {
                            GroupDetail srcGroupDetail = new GroupDetail();
                            srcGroupDetail.setGroupName(groupName);
                            srcGroupDetail.setEntityName(CollectionUtil.findMapValue(CommonFields.SRC_NAME.getValue(), path));
                            srcGroupDetail.setType(groupType);
                            srcGroupDetail.setEntityId(path.get(CommonFields._FROM.getValue()).toString());
                            srcGroupDetail.setBelongInner(Boolean.parseBoolean(path.get(CommonFields.SRC_BELONG_INNER.getValue()) == null ? "" : path.get(CommonFields.SRC_BELONG_INNER.getValue()).toString()));
                            addOneGroupDetail(batchInsertGroupDetailList, currentGroupEntityIds, srcGroupDetail);
                            if (srcGroupDetail.getBelongInner()) {
                                belongInnerEntityIds.add(srcGroupDetail.getEntityId());
                                if (srcGroupDetail.getEntityName().equalsIgnoreCase(groupName)) {
                                    companyGroup.setBelongInner(true); //核心企业是行内客户，则认为当前族谱也是行内客户
                                }
                            }

                        }


                        if (path.get(CommonFields._TO.getValue()) != null) {
                            GroupDetail dstGroupDetail = new GroupDetail();
                            dstGroupDetail.setGroupName(groupName);
                            dstGroupDetail.setEntityName(CollectionUtil.findMapValue(CommonFields.DST_NAME.getValue(), path));
                            dstGroupDetail.setType(groupType);
                            dstGroupDetail.setEntityId(path.get(CommonFields._TO.getValue()).toString());
                            dstGroupDetail.setBelongInner(Boolean.parseBoolean(path.get(CommonFields.DST_BELONG_INNER.getValue()) == null ? "" : path.get(CommonFields.DST_BELONG_INNER.getValue()).toString()));
                            addOneGroupDetail(batchInsertGroupDetailList, currentGroupEntityIds, dstGroupDetail);
                            if (dstGroupDetail.getBelongInner()) {
                                belongInnerEntityIds.add(dstGroupDetail.getEntityId());
                                if (dstGroupDetail.getEntityName().equalsIgnoreCase(groupName)) {
                                    companyGroup.setBelongInner(true); //核心企业是行内客户，则认为当前族谱也是行内客户
                                }
                            }
                        }

                        if (batchInsertGroupDetailList.size() >= batchInsertSize) {
                            groupDetailRepo.batchInsert(batchInsertGroupDetailList);
                            batchInsertGroupDetailList.clear();
                        }

                    }
                    companyGroup.setGroupName(groupName);
                    companyGroup.setPaths(pathInfos);
                    companyGroup.setType(groupType);
                    companyGroup.setEntityCount(currentGroupEntityIds.size());
                    companyGroup.setInnerEntityCount(belongInnerEntityIds.size());
                    companyGroup.setSubType(subType);
                    companyGroup.setBelongInner(false);
                    groupList.add(companyGroup);

                    if (groupList.size() >= batchInsertSize) {
                        companyGroupRepo.batchInsert(groupList);
                        log.info("CompanyGroup insert from {} to {}", counter * batchInsertSize, (counter + 1) * batchInsertSize);
                        groupList.clear();
                        counter += 1;
                    }
                }
                line = reader.readLine();
            }  //while循环结束


            if (batchInsertGroupDetailList.size() > 0) {

                groupDetailRepo.batchInsert(batchInsertGroupDetailList);
            }

            if (groupList.size() > 0) {
                log.info("CompanyGroup insert from {} to {} ", counter * batchInsertSize, counter * batchInsertSize + groupList.size());
                companyGroupRepo.batchInsert(groupList);
            }

            log.info("CompanyGroup insert finished");

        } catch (Exception e) {
            log.error("{}", e);
        }
    }

    /**
     * @description 向族谱实体列表中加入新的实体，已加入的不再加入
     * @param batchInsertGroupDetailList 已有的实体列表
     * @param newGroupDetail 准备新加入的实体
     * @return true 加入成功  false 加入失败（已存在或参数非法）
     * @author LewisLouis
     * @date 2018/8/15
     */
    private void addOneGroupDetail(List<GroupDetail> batchInsertGroupDetailList,Set<String> currentGroupEntityIds, GroupDetail newGroupDetail){

        if (null == newGroupDetail){
            return;
        }

        //如果当前实体，已经记录到当前族谱中，则跳过
        if (currentGroupEntityIds.contains(newGroupDetail.getEntityId())){
            return;
        }
        currentGroupEntityIds.add(newGroupDetail.getEntityId());
//
//        for (GroupDetail groupDetail:batchInsertGroupDetailList) {
//            if (groupDetail.getEntityId().equalsIgnoreCase(newGroupDetail.getEntityId())){
//                return;
//            }
//        }

        batchInsertGroupDetailList.add(newGroupDetail);
    }

}


