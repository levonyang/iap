package com.haizhi.iap.search.service;

import org.apache.commons.lang3.tuple.Pair;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by chenbo on 2017/8/24.
 */
public interface GraphClustersService {

    void importGroupsFromInput(InputStream inputStream, String type);

    List<Map<String, Object>> countByType();

    Pair<List<Map<String, Object>>, List<Map<String, Object>>> processPaths(String name);

    List<Map> queryPersonMembers(String groupName);

}
