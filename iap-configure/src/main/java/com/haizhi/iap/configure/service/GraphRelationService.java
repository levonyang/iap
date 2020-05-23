package com.haizhi.iap.configure.service;

import com.haizhi.iap.configure.model.GraphRelation;
import com.haizhi.iap.configure.model.GraphResult;
import com.haizhi.iap.configure.model.SearchGraphParam;

import java.util.List;
import java.util.Map;

public interface GraphRelationService {

    GraphRelation saveGraphRelation(GraphRelation graphRelation) throws Exception;

    List<GraphRelation> getGraphRelation(Integer graphId);

    boolean deleteGraphRelation(int id);

    GraphRelation getGraphConfig(String tableName);

    GraphResult queryGraphDetail(SearchGraphParam searchGraphParam);

    List<Map> getDemo(String graphName);

    Map getDemoGRelation(Integer grelationID);
}
