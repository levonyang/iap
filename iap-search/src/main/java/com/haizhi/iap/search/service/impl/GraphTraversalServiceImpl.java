package com.haizhi.iap.search.service.impl;

import com.haizhi.iap.search.controller.model.EdgesOption;
import com.haizhi.iap.search.controller.model.GraphOptions;
import com.haizhi.iap.search.model.GraphEdge;
import com.haizhi.iap.search.model.TraversalMeta;
import com.haizhi.iap.search.repo.GraphRepo;
import com.haizhi.iap.search.service.GraphTraversalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class GraphTraversalServiceImpl implements GraphTraversalService {

    @Autowired
    private GraphRepo graphRepo;

    @Override
    public TraversalMeta expandOneLevel(TraversalMeta meta, GraphOptions options, GraphEdge.Direction direction) {
        List<EdgesOption> edgesOptions = options.getEdges();
        return null;
    }
}
