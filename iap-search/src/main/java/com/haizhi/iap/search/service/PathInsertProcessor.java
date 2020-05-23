package com.haizhi.iap.search.service;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.haizhi.iap.search.model.ClusterPath;
import com.haizhi.iap.search.model.EntityPathMap;
import com.haizhi.iap.search.repo.ClusterPathRepo;
import com.haizhi.iap.search.repo.EntityPathMapRepo;
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
import java.util.UUID;

/**
 * Created by chenbo on 2017/8/28.
 */
@Slf4j
@Service
public class PathInsertProcessor implements Processor {

    @Setter
    @Autowired
    ObjectMapper objectMapper;

    @Setter
    @Autowired
    ClusterPathRepo clusterPathRepo;

    @Setter
    @Autowired
    EntityPathMapRepo entityPathMapRepo;

    private Integer DEFAULT_PAGE_SIZE = 500;

    public void process(Exchange exchange) throws Exception {
        BufferedReader reader = null;
        Long domainId = null;
        if (exchange.getIn().getBody() != null && exchange.getIn().getHeader("domainId") != null) {
            reader = (BufferedReader) exchange.getIn().getBody();
            domainId = (Long) exchange.getIn().getHeader("domainId");
        }
        clusterPathRepo.delete(domainId);
        entityPathMapRepo.delete(domainId);
        log.info("delete cluster_path of domain: {}", domainId);

        String line = null;
        try {
            line = reader.readLine();
            List<ClusterPath> pathList = Lists.newArrayList();
            List<EntityPathMap> mapList = Lists.newArrayList();
            Integer counter = 0;
            while (line != null) {
                if (line.indexOf("type") > 0 && line.indexOf("\"type\"") <= 0) {
                    line = line.replace("type", "\"type\"");
                }
                if (line.indexOf("paths") > 0 && line.indexOf("\"paths\"") <= 0) {
                    line = line.replace("paths", "\"paths\"");
                }
                Map<String, Object> data = objectMapper.readValue(line, Map.class);

                ClusterPath onePath = new ClusterPath();
                onePath.setDomainId(domainId);
                onePath.setPathId(UUID.randomUUID().toString().toLowerCase().replaceAll("-", ""));
                onePath.setType(data.get("type") == null ? "" : data.get("type").toString());
                onePath.setPaths(data.get("paths") == null ? "" : JSON.toJSONString(data.get("paths")));

                //插入映射
                if (data.get("paths") != null && data.get("paths") instanceof List) {
                    List<Map<String, Object>> paths = (List<Map<String, Object>>) data.get("paths");
                    for (Map<String, Object> path : paths) {
                        EntityPathMap map1 = new EntityPathMap();
                        map1.setDomainId(domainId);
                        map1.setPathId(onePath.getPathId());
                        if (path.get("_from") != null) {
                            map1.setEntityId(path.get("_from").toString());
                        }

                        EntityPathMap map2 = new EntityPathMap();
                        map2.setDomainId(domainId);
                        map2.setPathId(onePath.getPathId());
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
                            entityPathMapRepo.batchInsert(mapList);
                            mapList = Lists.newArrayList();
                        }
                    }
                }

                pathList.add(onePath);

                if (pathList.size() >= DEFAULT_PAGE_SIZE) {
                    log.info("cluster_path insert from {} to {}", counter * DEFAULT_PAGE_SIZE, (counter + 1) * DEFAULT_PAGE_SIZE);
                    clusterPathRepo.batchInsert(pathList);
                    pathList = Lists.newArrayList();
                    counter += 1;
                }
                line = reader.readLine();
            }
            log.info("cluster_path insert from {} to {} ", counter * DEFAULT_PAGE_SIZE, counter * DEFAULT_PAGE_SIZE + pathList.size());
            clusterPathRepo.batchInsert(pathList);
            entityPathMapRepo.batchInsert(mapList);
            log.info("cluster_path insert finished");
        } catch (IOException e) {
            log.error("{}", e);
        }
    }

}
