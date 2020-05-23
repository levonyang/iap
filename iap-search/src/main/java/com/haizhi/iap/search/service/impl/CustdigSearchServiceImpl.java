package com.haizhi.iap.search.service.impl;

import com.haizhi.iap.common.bean.CustdigParam;
import com.haizhi.iap.search.model.GraphEdge;
import com.haizhi.iap.search.repo.atlas.AtlasGraphRepo;
import com.haizhi.iap.search.service.CustdigSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author mtl
 * @Description:
 * @date 2020/3/11 11:09
 */
@Slf4j
@Service
public class CustdigSearchServiceImpl implements CustdigSearchService {

    @Autowired
    private AtlasGraphRepo graphRepo;

    @Override
    public Object searchTravel(CustdigParam param) {
        List<String> companys = param.getCompanys();
        String type = param.getType();
        int depth = param.getDepth();
        List<Map<String, Object>> companyids = graphRepo.getCompanyByNames(companys);
        List<Map<String, Object>> personids = graphRepo.getPersonByNames(companys);

        Set<String> idSet = new HashSet<>();
        for (Map idmap : companyids) {
            idSet.add(idmap.get("id").toString());
        }
        for (Map idmap : personids) {
            idSet.add(idmap.get("id").toString());
        }
        GraphEdge.Direction direct;
        if("in".equals(param.getDirect())){
            direct = GraphEdge.Direction.IN;
        }else if("any".equals(param.getDirect())){
            direct = GraphEdge.Direction.ALL;
        }else{
            direct = GraphEdge.Direction.OUT;
        }
        List<Map<String, Object>> result = graphRepo.traversalGraphToGetPath("tv_person,tv_company",
                type, depth, idSet, idSet, direct);

        List<Map> finaledges = new ArrayList<>();
        List<Map> finalvertices = new ArrayList<>();
        Set<String> finalidSet = new HashSet<>();
        for (Map map : result) {
            List<Map> edges = (List<Map>) map.get("edges");
            for (Map edge : edges) {
                String edgid = (String) edge.get("_id");
                boolean add = finalidSet.add(edgid);
                if(add){
                    finaledges.add(edge);
                }
            }
            List<Map> vertices = (List<Map>) map.get("vertices");
            for (Map vertex : vertices) {
                String vertexid = (String) vertex.get("_id");
                boolean add = finalidSet.add(vertexid);
                if(add){
                    finalvertices.add(vertex);
                }
            }
        }
        List<String> singleEntitys = new ArrayList<>();
        for (String id : idSet) {
            if(!finalidSet.contains(id)){
                singleEntitys.add(id);
            }
        }
        List<Map<String, Object>> companyinfos = graphRepo.getCompanyByIds(singleEntitys);
        List<Map<String, Object>> personinfos = graphRepo.getPersonByIds(singleEntitys);
        finalvertices.addAll(companyinfos);
        finalvertices.addAll(personinfos);
        Map graph = new HashMap();
        graph.put("edges",finaledges);
        graph.put("vertices",finalvertices);
        return graph;
    }

    public Object searchShortPath(CustdigParam param){
        List<String> companys = param.getCompanys();
        String type = param.getType();
        int depth = param.getDepth();
        //把中文转化为id
        List<Map<String, Object>> companyids = graphRepo.getCompanyByNames(companys);
        List<Map<String, Object>> personids = graphRepo.getPersonByNames(companys);

        Set<String> idSet = new HashSet<>();
        for (Map idmap : companyids) {
            idSet.add(idmap.get("id").toString());
        }
        for (Map idmap : personids) {
            idSet.add(idmap.get("id").toString());
        }
        GraphEdge.Direction direct;
        if("in".equals(param.getDirect())){
            direct = GraphEdge.Direction.IN;
        }else if("any".equals(param.getDirect())){
            direct = GraphEdge.Direction.ALL;
        }else{
            direct = GraphEdge.Direction.OUT;
        }
        Object[] objects = idSet.toArray();
        int length = objects.length;
        List<Map> vertices = new ArrayList<>();
        List<Map> edges = new ArrayList<>();
        Set<String> vidSets = new HashSet<>();
        Set<String> eidSets = new HashSet<>();
//        Set<String> singlevidSet = new HashSet<>();
        //实体进行两两最短路径查找
        for (int i = 0; i < length; i++) {
            String start_id = (String) objects[i]; //节点1
            for (int j = i + 1; j < length; j++) {
                String end_id = (String) objects[j]; //节点2
                //寻找两点的最短路径
                log.info("search short path <{},{}>",start_id,end_id);
                List<Map<String, Object>> shortPath = graphRepo.shortPath("tv_person,tv_company", type, start_id, end_id, direct, "");
                if(null != shortPath && shortPath.size() <= (depth + 1)){ //例如：depth = 5度,则数组的长度不得超过6
//                    singlevidSet.remove(start_id); //从孤点组里移除
//                    singlevidSet.remove(end_id); //从孤点组里移除
                    for (Map<String, Object> path : shortPath) {
                        Object edgeObj = path.get("edge");
                        if(null != edgeObj){
                            Map edge = (Map) edgeObj;
                            String id = getFromMap(edge,"_id",null);
                            if(eidSets.add(id)){ //避免重复加入
                                edges.add(edge);
                            }
                        }
                        Object nodeObj = path.get("node");
                        if(null != nodeObj){
                            Map node = (Map) nodeObj;
                            String id = getFromMap(node,"_id",null);
                            if(vidSets.add(id)){ //避免重复加入
                                vertices.add(node);
                            }
                        }
                    }
                }/*else{ //如果数组长度超过depth+1,则视为孤点
                    if(!vidSets.contains(start_id)){
                        singlevidSet.add(start_id); //添加到孤点组
                    }
                    if(!vidSets.contains(end_id)){
                        singlevidSet.add(end_id); //添加到孤点组
                    }
                }*/
            }
        }
        List<String> singleEntitys = new ArrayList<>();
        for (String id : idSet) { //vidSets里不存在的实体，都看作孤点
            if(!vidSets.contains(id)){
                singleEntitys.add(id);
            }
        }
        List<Map<String, Object>> companyinfos = graphRepo.getCompanyByIds(singleEntitys);
        List<Map<String, Object>> personinfos = graphRepo.getPersonByIds(singleEntitys);
        vertices.addAll(companyinfos);
        vertices.addAll(personinfos);
        Map graph = new HashMap();
        graph.put("edges",edges);
        graph.put("vertices",vertices);
        return graph;
    }

    private String getFromMap(Map data, String key, String els) {
        Object obj = data.get(key);
        if(null == obj){
            return els;
        }
        return obj.toString();
    }

    @Override
    public Set<String> findCustByname(List<String> companys) {
        List<Map<String, Object>> companyids = graphRepo.getCompanyByNames(companys);
        List<Map<String, Object>> personids = graphRepo.getPersonByNames(companys);
        Set<String> idSet = new HashSet<>();
        for (Map idmap : companyids) {
            idSet.add(idmap.get("id").toString());
        }
        for (Map idmap : personids) {
            idSet.add(idmap.get("id").toString());
        }
        return idSet;
    }
}
