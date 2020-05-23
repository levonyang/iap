package com.haizhi.iap.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.haizhi.iap.search.model.ClusterDomain;
import com.haizhi.iap.search.model.ClusterGroup;
import com.haizhi.iap.search.repo.ClusterDomainRepo;
import com.haizhi.iap.search.repo.ClusterGroupRepo;
import com.haizhi.iap.search.repo.ClusterPathRepo;
import com.haizhi.iap.search.repo.CompanyGroupRepo;
import com.haizhi.iap.search.repo.EnterpriseRepo;
import com.haizhi.iap.search.repo.EntityClusterMapRepo;
import com.haizhi.iap.search.repo.EntityPathMapRepo;
import com.haizhi.iap.search.repo.GroupDetailRepo;
import com.haizhi.iap.search.repo.RedisRepo;
import com.haizhi.iap.search.service.GraphClustersService;
import com.haizhi.iap.search.service.GraphService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by chenbo on 2017/8/24.
 */
@Slf4j
@Service
public class GraphClustersServiceImpl implements GraphClustersService {

    @Setter
    @Autowired
    @Qualifier("producerTemplate")
    ProducerTemplate template;

    @Setter
    @Autowired
    ClusterDomainRepo clusterDomainRepo;

    @Setter
    @Autowired
    ClusterGroupRepo clusterGroupRepo;

    @Setter
    @Autowired
    EntityClusterMapRepo entityClusterMapRepo;

    @Setter
    @Autowired
    ClusterPathRepo clusterPathRepo;

    @Setter
    @Autowired
    EntityPathMapRepo entityPathMapRepo;

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

    @Setter
    @Autowired
    EnterpriseRepo enterpriseRepo;


    @Override
    public void importGroupsFromInput(InputStream inputStream, String type) {
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader reader = new BufferedReader(inputStreamReader);

            importGroupsFromReader(reader, type);

        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> countByType() {
        return companyGroupRepo.countByType();
    }

    /**
     * @param groupName
     * @return java.util.List<java.util.Map>
     * @description 通过集团名称查询该集团所有的自然人列表
     * @author yuding
     * @date 2018/8/24
     */
    @Override
    public List<Map> queryPersonMembers(String groupName) {
        //获取该集团的paths
        List<Map<String, Object>> edges = Lists.newArrayList();

        String paths = companyGroupRepo.findByGroupName(groupName);
        List<Map> groupPaths = Lists.newArrayList();
        try {
            groupPaths = objectMapper.readValue(paths, List.class);
        } catch (IOException ex) {
            log.error("{}", ex);
        }
        List<Map> groupPerson = queryGroupPerson(groupPaths);
        return groupPerson;
    }


    /**
     * @param groupPaths
     * @return java.util.List<java.util.Map>
     * @description 通过遍历paths查询该集团所有的自然人
     * @author yuding
     * @date 2018/8/24
     */
    public List<Map> queryGroupPerson(List<Map> groupPaths) {
        //存储整个集团的自然人
        List<Map> groupPerson = new ArrayList<>();
        //存储自然人的字典供后面判断是否已经遍历过
        Map<String, Map> personDict = new HashMap<>();

        for (int i = 0; i < groupPaths.size(); i++) {
            //存储自然人的详细信息
            Map<String, Object> person = new HashMap<>();
            Map<String, Object> map = groupPaths.get(i);

            String groupSrcPersonid = map.get("_from").toString();
            String personSrc = "src_";
            traversePerson(groupSrcPersonid, personDict, map, person, groupPerson, personSrc);
            String groupDstPersonid = map.get("_to").toString();
            String personDst = "dst_";
            traversePerson(groupDstPersonid, personDict, map, person, groupPerson, personDst);
        }
        return groupPerson;
    }

    //遍历每条边 并将对应信息存储list<map>里面
    public void traversePerson(String groupPersonid,
                               Map<String, Map> personDict,
                               Map<String, Object> map,
                               Map<String, Object> person,
                               List<Map> groupPerson,
                               String personType) {
        if (groupPersonid.startsWith("Person")) {
            //判断该自然人是否已经遍历过
            if (!personDict.containsKey(groupPersonid)) {
                for (java.lang.Object oneKey : map.keySet()) {
                    if (oneKey.toString().startsWith(personType)) {
                        person.put(oneKey.toString().substring(4), map.get(oneKey));
                    }
                }
                person.put("personId", groupPersonid);
                personDict.put(groupPersonid, person);
                groupPerson.add(person);
            }
        }

    }




    public ClusterDomain getDomain(String domainName, String type) {
        return clusterDomainRepo.find(domainName, type);
    }



    private ClusterGroup processClusterGroup(ClusterGroup group) {
        if (group == null || Strings.isNullOrEmpty(group.getPaths())) {
            return group;
        }
        Pair<List<Map<String, Object>>, List<Map<String, Object>>> pair = processPaths(group.getPaths());
        group.setVertexes(pair.getLeft());
        group.setPaths(JSON.toJSONString(pair.getRight()));
        return group;
    }

    public void importGroupsFromReader(BufferedReader reader, String type) {
        template.asyncRequestBodyAndHeader("direct:group_insert", reader, "type", type);
    }


    /**
    * @description
    * @param paths
    * @return org.apache.commons.lang3.tuple.Pair<java.util.List<java.util.Map<java.lang.String,java.lang.Object>>,java.util.Map<java.lang.String,java.util.Map<java.lang.String,java.lang.Object>>>
    * @author liulu
    * @date 2018/12/19
    */
    public Pair<List<Map<String, Object>>, Map<String, Map<String, Object>>> processPathsForClusterCompany(String paths) {
        Set<Map<String, Object>> vertexes = Sets.newHashSet();
        List<Map<String, Object>> edges = Lists.newArrayList();
        Map<String, Map<String, Object>> companyMap = Maps.newHashMap();
        try {
            log.info("paths: {}", paths);
            edges = objectMapper.readValue(paths, List.class);
            List<String> isExistId = new ArrayList<>();
            for (Map<String, Object> edge : edges) {
                if (edge.get("_from") != null) {
                    if (!isExistId.contains(edge.get("_from")) && edge.get("_from").toString().startsWith("Company")) {
                        Map<String, Object> company = new LinkedHashMap<String, Object>();
                        for (String oneKey : edge.keySet()) {
                            if (oneKey.startsWith("src_")) {
                                company.put(oneKey.substring(4), edge.get(oneKey));
                            }
                        }
                        company.put("_id", edge.getOrDefault("_from", ""));
                        edge.put("_from_id", company.get("_id"));
                        vertexes.add(company);
                        companyMap.put(company.get("name").toString(), company);
                        isExistId.add(edge.get("_from").toString());
                    }
                }
                if (edge.get("_to") != null) {
                    if (!isExistId.contains(edge.get("_to")) && edge.get("_to").toString().startsWith("Company")) {
                        Map<String, Object> company = new LinkedHashMap<String, Object>();
                        for (String oneKey : edge.keySet()) {
                            if (oneKey.startsWith("dst_")) {
                                company.put(oneKey.substring(4), edge.get(oneKey));
                            }
                        }
                        company.put("_id", edge.getOrDefault("_to", ""));
                        edge.put("_to_id", company.get("_id"));
                        vertexes.add(company);
                        companyMap.put(company.get("name").toString(), company);
                        isExistId.add(edge.get("_to").toString());
                    }
                }
            }
        } catch (IOException ex) {
            log.error("{}", ex);
        }
        log.info("ve:{} map: {}", vertexes, companyMap);
        return Pair.of(Lists.newArrayList(vertexes), companyMap);
    }

    public Pair<List<Map<String, Object>>, List<Map<String, Object>>> processPaths(String paths) {
        Set<Map<String, Object>> vertexes = Sets.newHashSet();
        List<Map<String, Object>> edges = Lists.newArrayList();
        try {
            edges = objectMapper.readValue(paths, List.class);
            List<String> isExistId = new ArrayList<>();
            for (Map<String, Object> edge : edges) {
                if (edge.get("_from") != null) {
                    if (!isExistId.contains(edge.get("_from"))) {
                        Map<String, Object> company = new LinkedHashMap<String, Object>();
                        for (String oneKey : edge.keySet()) {
                            if (oneKey.startsWith("src_")) {
                                company.put(oneKey.substring(4), edge.get(oneKey));
                            }
                        }
                        company.put("_id", edge.getOrDefault("_from", ""));
//                        company.put("name", edge.getOrDefault("src_name", ""));
//                        company.put("business_status",  edge.getOrDefault("src_status", ""));
//                        company.put("belong_inner",  edge.getOrDefault("src_belong_inner", false));
                        edge.put("_from_id", company.get("_id"));
                        vertexes.add(company);
                        isExistId.add(edge.get("_from").toString());
                    }
                }
                if (edge.get("_to") != null) {
                    if (!isExistId.contains(edge.get("_to"))) {
                        Map<String, Object> company = new LinkedHashMap<String, Object>();
                        for (String oneKey : edge.keySet()) {
                            if (oneKey.startsWith("dst_")) {
                                company.put(oneKey.substring(4), edge.get(oneKey));
                            }
                        }
                        company.put("_id", edge.getOrDefault("_to", ""));
//                        company.put("name", edge.getOrDefault("dst_name", ""));
//                        company.put("business_status", edge.getOrDefault("dst_status", ""));
//                        company.put("belong_inner",  edge.getOrDefault("dst_belong_inner", false));
                        edge.put("_to_id", company.get("_id"));
                        vertexes.add(company);
                        isExistId.add(edge.get("_to").toString());
                    }
                }
//                if (edge.get("_from") != null) {
//                    //这里的from是名字,根据名字查id
//                    Map<String, Object> company = graphService.brief(edge.get("_from").toString());
//                    if (company != null && company.get("_id") != null) {
//                        edge.put("_from_id", company.get("_id"));
//                        vertexes.add(company);
//                    }
//                }
//
//                if (edge.get("_to") != null) {
//                    Map<String, Object> company = graphService.brief(edge.get("_to").toString());
//                    if (company != null && company.get("_id") != null) {
//                        edge.put("_to_id", company.get("_id"));
//                        vertexes.add(company);
//                    }
//                }
            }
        } catch (IOException ex) {
            log.error("{}", ex);
        }
        return Pair.of(Lists.newArrayList(vertexes), edges);
    }
}
