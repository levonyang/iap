package com.haizhi.iap.configure.service.impl;

import com.haizhi.iap.common.utils.GraphUtil;
import com.haizhi.iap.configure.model.GraphRelation;
import com.haizhi.iap.configure.model.GraphResult;
import com.haizhi.iap.configure.model.SearchGraphParam;
import com.haizhi.iap.configure.repo.GraphRelationRepo;
import com.haizhi.iap.configure.service.GraphRelationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GraphRelationServiceImpl implements GraphRelationService {

    @Autowired
    GraphRelationRepo graphRelationRepo;

    @Override
    @Transactional
    public GraphRelation saveGraphRelation(GraphRelation graphRelation) throws Exception {
        if (graphRelation.getId() == null && (graphRelation.getSourceConfigId() != null || graphRelation.getName() != null)) {
            // create
            GraphRelation graphRelationOld = graphRelationRepo.getGraphRelationByIdOrName(graphRelation.getSourceConfigId(), graphRelation.getName());
            if (graphRelationOld != null && graphRelationOld.getName() != null &&
                    graphRelationOld.getName().equals(graphRelation.getName()) ) {
                throw new Exception("保存失败，关系名称重复");
            } else if (graphRelationOld != null && graphRelationOld.getSourceConfigId() != null &&
                    graphRelationOld.getSourceConfigId().equals(graphRelation.getSourceConfigId())) {
                throw new Exception("保存失败，主题名称已被使用");
            }
        } else if (graphRelation.getId() != null && (graphRelation.getSourceConfigId() != null || graphRelation.getName() != null)) {
            // update
            GraphRelation graphRelationOld = graphRelationRepo.getGraphRelationByIdOrNameFilterId(graphRelation.getSourceConfigId(),
                    graphRelation.getName(), graphRelation.getId());
            if (graphRelationOld != null && graphRelationOld.getName() != null &&
                    graphRelationOld.getName().equals(graphRelation.getName()) ) {
                throw new Exception("保存失败，关系名称重复");
            } else if (graphRelationOld != null && graphRelationOld.getSourceConfigId() != null &&
                    graphRelationOld.getSourceConfigId().equals(graphRelation.getSourceConfigId())) {
                throw new Exception("保存失败，主题名称已被使用");
            }
        }
        return graphRelationRepo.saveGraphRelation(graphRelation);
    }

    @Override
    public List<GraphRelation> getGraphRelation(Integer graphId) {
        return graphRelationRepo.getGraphRelation(graphId);
    }

    @Override
    public boolean deleteGraphRelation(int id) {
        int ret = graphRelationRepo.deleteRelation(id);
        return ret > 0 ? true : false;
    }

    @Override
    public GraphRelation getGraphConfig(String tableName) {
        List<GraphRelation> ret = graphRelationRepo.getGraphConfig(tableName);
        return ret.size() > 0 ? ret.get(0) : null;
    }

    @Override
    public GraphResult queryGraphDetail(SearchGraphParam searchGraphParam) {
        return graphRelationRepo.queryGraphDetail(searchGraphParam);
    }

    @Override
    public List<Map> getDemo(String graphName) {
        return graphRelationRepo.queryForDemo(graphName);
    }

    @Override
    public Map getDemoGRelation(Integer grelationID) {
        Map<String,Object> tableNameAndLabel = graphRelationRepo.getDSConfigTableNameByGRelation(grelationID);

        String tableName = null;
        String label = null;
        if (tableNameAndLabel != null) {
            tableName = tableNameAndLabel.get("tableName").toString();
            label = tableNameAndLabel.get("label").toString();
        }
        if (!StringUtils.isEmpty(tableName)) {
            Map doc = this.graphRelationRepo.getRandomSingleDoc(tableName);
            List<Map> path = this.graphRelationRepo.getRandomSinglePath(tableName,doc.get("_from").toString());
            if (path != null) {
                Map<String,List> d3Path = GraphUtil.buildArangoPathForD3(path);
                List<Map> edges = d3Path.get("edges");
                for (Map edge : edges) {
                    edge.put("label",label);
                }
                return d3Path;
            }
        }
        return null;
    }

}
