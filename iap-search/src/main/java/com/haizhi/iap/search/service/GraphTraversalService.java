package com.haizhi.iap.search.service;

import com.haizhi.iap.search.controller.model.GraphOptions;
import com.haizhi.iap.search.model.GraphEdge;
import com.haizhi.iap.search.model.TraversalMeta;

public interface GraphTraversalService {

    /**
     * 展开一层
     *
     * @param meta 展开的顶点信息
     * @param options 过滤项
     * @param direction 边的方向
     * @return 新的一层顶点信息
     */
    TraversalMeta expandOneLevel(TraversalMeta meta, GraphOptions options, GraphEdge.Direction direction);

}
