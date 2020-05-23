package com.haizhi.iap.search.service;

import com.haizhi.iap.search.controller.model.Graph;

import java.util.List;

/**
 * Created by chenbo on 2017/8/9.
 */
public interface GraphExplainService {

    Graph explainActualControl(String rule, List<String> fromList, List<String> depthList, String to);

    Graph explainConcert(String from, String to, String target, String rule);
}
