package com.haizhi.iap.search.controller.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.haizhi.iap.search.repo.GraphRepo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Created by chenbo on 17/2/23.
 */
@Data
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class Graph {

    List vertexes;

    List edges;

    Boolean found;

    Integer length;

    /**
     * 本来不用传GraphRepo过来的,但是需求要对边做去重,需要再次查一些东西
     * 所以就有这么蹩脚的写法
     */
    public static Graph genVirtualGraph(Map<String, Object> vertexMap, Map<String, Object> edgeMap, GraphRepo graphRepo) {
        return genVirtualGraph(vertexMap, edgeMap, null, null, graphRepo);
    }

    public static Graph genVirtualGraph(Map<String, Object> vertexMap, Map<String, Object> edgeMap, Boolean found, Integer length, GraphRepo graphRepo) {
        TreeMap<String, Object> sortedVertexMap = new TreeMap<>(vertexMap);
        TreeMap<String, Object> sortedEdgeMap = new TreeMap<>(edgeMap);

        for (String key : sortedVertexMap.keySet()) {
            Map<String, Object> vertex = (Map<String, Object>) sortedVertexMap.get(key);
            vertex.put("type", "");
            vertex.put("getPathTypes", 0);
        }

        Map<String, Object> addedEdgeMap = Maps.newHashMap();
        for (String key : sortedEdgeMap.keySet()) {
            Map<String, Object> edge = (Map<String, Object>) sortedEdgeMap.get(key);
            if (edge.get("label") != null && edge.get("label").equals("一致行动关系")) {
                //rule = "Rule3" 就不用管
                // 则画_from , _to 和 target 的三角，  _from , _to 都指向  target
                if (edge.get("rule") != null && !edge.get("rule").equals("Rule3") && edge.get("target") != null) {
                    Map<String, Object> fromMap = Maps.newHashMap();
                    String addedId = "concert/" + UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");
                    fromMap.put("_id", addedId);
                    fromMap.put("_from", edge.get("_from"));
                    fromMap.put("_to", edge.get("target"));
                    fromMap.put("label", "一致行动关系");
                    addedEdgeMap.put(addedId, fromMap);

                    Map<String, Object> toMap = Maps.newHashMap();
                    addedId = "concert/" + UUID.randomUUID().toString().toLowerCase().replaceAll("-", "");
                    toMap.put("_id", addedId);
                    toMap.put("_from", edge.get("_to"));
                    toMap.put("_to", edge.get("target"));
                    toMap.put("label", "一致行动关系");
                    addedEdgeMap.put(addedId, toMap);

                    if (sortedVertexMap != null && !sortedVertexMap.keySet().contains(edge.get("target"))) {
                        String target = edge.get("target").toString();
                        //target不在顶点里,就生成一个顶点
                        //获取name
                        Map<String, Object> company = graphRepo.fetchDocument(target);
                        if (company != null && company.get("name") != null) {
                            Map<String, Object> addedVertex = Maps.newHashMap();
                            addedVertex.put("entity_type", target.split("/")[0]);
                            addedVertex.put("getPathTypes", 0);
                            addedVertex.put("type", "");
                            addedVertex.put("name", company.get("name"));

                            addedVertex.put("_id", target);
                            addedVertex.put("_type", target.split("/")[0]);

                            sortedVertexMap.put(target, addedVertex);
                        }
                    }
                }
            }
        }
        if (addedEdgeMap.size() > 0) {
            sortedEdgeMap.putAll(addedEdgeMap);
        }

        //边去重处理
        Iterator<String> keyIt = sortedEdgeMap.keySet().iterator();
        List<TempEdge> filter = Lists.newArrayList();
        while (keyIt.hasNext()) {
            String key = keyIt.next();
            if (sortedEdgeMap.get(key) instanceof Map) {
                TempEdge tempEdge = new TempEdge((Map<String, Object>) sortedEdgeMap.get(key));
                if (!filter.contains(tempEdge)) {
                    filter.add(tempEdge);
                } else {
                    //针对不同类型有不同的处理逻辑
                    //一致行动关系,后面相同的直接移除
                    TempEdge repliEdge = filter.get(filter.indexOf(tempEdge));
                    if (repliEdge.getType().equalsIgnoreCase("Concert")) {
                        //target不同不做合并
                        if(!(repliEdge.getTarget() == null && tempEdge.getTarget() == null)){
                            if(repliEdge.getTarget() == null || tempEdge.getTarget() == null){
                                continue;
                            }else if(!repliEdge.getTarget().equals(tempEdge.getTarget())){
                                continue;
                            }
                        }
                        keyIt.remove();
                    } else if (repliEdge.getType().equalsIgnoreCase("Officer")) {
                        String label = ((Map<String, Object>) sortedEdgeMap.get(repliEdge.getId())).get("label").toString();
                        String newLabel = "";
                        if (label.indexOf("）") < 0) {
                            newLabel = label + "（" + ((Map<String, Object>) sortedEdgeMap.get(key)).get("position") + "）";
                        } else {
                            newLabel = label.substring(0, label.lastIndexOf("）")) + "," + ((Map<String, Object>) sortedEdgeMap.get(key)).get("position") + "）";
                        }
                        ((Map<String, Object>) sortedEdgeMap.get(repliEdge.getId())).put("label", newLabel);
                        keyIt.remove();
                    } else {
                        //TODO 其他情况处理
                        log.warn("type: {} 存在重复边,请处理.", filter.get(filter.indexOf(tempEdge)).getType());
                    }
                }
            }
        }

        List vertexes = Lists.newArrayList(sortedVertexMap.values());
        List edges = Lists.newArrayList(sortedEdgeMap.values());
        return new Graph(vertexes, edges, found, length);
    }
}

class TempEdge {
    private Object id;
    private String from;
    private String to;
    private String target;
    private String type;

    public TempEdge(Map<String, Object> edgeMap) {
        this.id = edgeMap.get("_id");
        this.from = edgeMap.get("_from") == null ? null : edgeMap.get("_from").toString();
        this.to = edgeMap.get("_to") == null ? null : edgeMap.get("_to").toString();
        this.target = edgeMap.get("target") == null ? null : edgeMap.get("target").toString();
        this.type = edgeMap.get("_id") == null ? null : edgeMap.get("_id").toString().split("/")[0];
    }

    public TempEdge(Object id, String from, String to, String label) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.target = target;
        this.type = label;
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TempEdge tempEdge = (TempEdge) o;

        if (!from.equals(tempEdge.from)) return false;
        if (!to.equals(tempEdge.to)) return false;
        return type != null ? type.equalsIgnoreCase(tempEdge.type) : tempEdge.type == null;

    }

    @Override
    public int hashCode() {
        int result = from.hashCode();
        result = 31 * result + to.hashCode();
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TempEdge{" +
                "id=" + id +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", target='" + target + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
