package com.haizhi.iap.search.service;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.haizhi.iap.common.utils.SecretUtil;
import com.haizhi.iap.search.model.ClusterGroup;
import com.haizhi.iap.search.model.EntityClusterMap;
import com.haizhi.iap.search.repo.EntityClusterMapRepo;
import com.haizhi.iap.search.repo.ClusterGroupRepo;
import com.haizhi.iap.search.repo.RedisRepo;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by chenbo on 2017/8/28.
 */
@Slf4j
@Service
public class ClusterInsertProcessor implements Processor {

    @Setter
    @Autowired
    ObjectMapper objectMapper;

    @Setter
    @Autowired
    ClusterGroupRepo clusterGroupRepo;

    @Setter
    @Autowired
    EntityClusterMapRepo entityClusterMapRepo;

    @Setter
    @Autowired
    RedisRepo redisRepo;

    private Integer DEFAULT_PAGE_SIZE = 500;

    public void process(Exchange exchange) throws Exception {
        BufferedReader reader = null;
        Long domainId = null;
        if (exchange.getIn().getBody() != null && exchange.getIn().getHeader("domainId") != null) {
            reader = (BufferedReader) exchange.getIn().getBody();
            domainId = (Long) exchange.getIn().getHeader("domainId");
        }
        clusterGroupRepo.delete(domainId);
        entityClusterMapRepo.delete(domainId);
        redisRepo.deleteGraphClusters(domainId);
        log.info("delete cluster_group of domain: {}", domainId);

        String line = null;
        try {
            line = reader.readLine();
            List<ClusterGroup> clusterList = Lists.newArrayList();
            List<EntityClusterMap> mapList = Lists.newArrayList();
            Integer counter = 0;
            while (line != null) {
                if (line.indexOf("group_name") > 0 && line.indexOf("\"group_name\"") <= 0) {
                    line = line.replace("group_name", "\"group_name\"");
                }
                if (line.indexOf("paths") > 0 && line.indexOf("\"paths\"") <= 0) {
                    line = line.replace("paths", "\"paths\"");
                }
                if (line.indexOf("type") > 0 && line.indexOf("\"type\"") <= 0) {
                    line = line.replace("type", "\"type\"");
                }
                Map<String, Object> data = objectMapper.readValue(line, Map.class);

                ClusterGroup oneGroup = new ClusterGroup();
                oneGroup.setDomainId(domainId);
                oneGroup.setClusterName(data.get("group_name") == null ? "" : data.get("group_name").toString());
                oneGroup.setClusterCid(data.get("group_name") == null ? "" : "Company/" + SecretUtil.md5(data.get("group_name").toString()));
                oneGroup.setPathCount(data.get("paths") == null || !(data.get("paths") instanceof List) ? 0 : ((List)data.get("paths")).size());
                oneGroup.setPaths(data.get("paths") == null ? "" : JSON.toJSONString(data.get("paths")));
                oneGroup.setType(data.get("type") == null ? "" : data.get("type").toString());

                //插入映射
                if (data.get("paths") != null && data.get("paths") instanceof List) {
                    List<Map<String, Object>> paths = (List<Map<String, Object>>) data.get("paths");
                    for (Map<String, Object> path : paths) {
                        EntityClusterMap map1 = new EntityClusterMap();
                        map1.setDomainId(domainId);
                        map1.setClusterCid(oneGroup.getClusterCid());
                        if (path.get("_from") != null) {
                            map1.setEntityId(path.get("_from").toString());
                        }

                        EntityClusterMap map2 = new EntityClusterMap();
                        map2.setDomainId(domainId);
                        map2.setClusterCid(oneGroup.getClusterCid());
                        if (path.get("_to") != null) {
                            map2.setEntityId(path.get("_to").toString());
                        }

                        if (map1.getEntityId() != null) {
                            mapList.add(map1);
                        }
                        if (map2.getEntityId() != null) {
                            mapList.add(map2);
                        }
                        if (mapList.size() >= DEFAULT_PAGE_SIZE) {
                            entityClusterMapRepo.batchInsert(mapList);
                            mapList = Lists.newArrayList();
                        }
                    }
                }

                clusterList.add(oneGroup);

                if (clusterList.size() >= DEFAULT_PAGE_SIZE) {
                    log.info("cluster_group insert from {} to {}", counter * DEFAULT_PAGE_SIZE, (counter + 1) * DEFAULT_PAGE_SIZE);
                    clusterGroupRepo.batchInsert(clusterList);
                    clusterList = Lists.newArrayList();
                    counter += 1;
                }
                line = reader.readLine();
            }
            log.info("cluster_group insert from {} to {} ", counter * DEFAULT_PAGE_SIZE, counter * DEFAULT_PAGE_SIZE + clusterList.size());
            clusterGroupRepo.batchInsert(clusterList);
            entityClusterMapRepo.batchInsert(mapList);
            log.info("cluster_group insert finished");
        } catch (IOException e) {
            log.error("{}", e);
        }
    }

}
