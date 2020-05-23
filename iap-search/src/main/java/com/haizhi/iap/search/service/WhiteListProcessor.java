package com.haizhi.iap.search.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.haizhi.iap.common.utils.SecretUtil;
import com.haizhi.iap.search.controller.model.Graph;
import com.haizhi.iap.search.controller.model.GraphReq;
import com.haizhi.iap.search.repo.RedisRepo;
import lombok.Setter;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by chenbo on 2017/7/22.
 */
@Service
public class WhiteListProcessor implements Processor {

    @Setter
    @Value("${white_list.switch}")
    Boolean switchOn;

    @Setter
    @Resource(name = "whiteList")
    List<String> whiteList;

    @Setter
    @Autowired
    GraphService graphService;

    @Setter
    @Autowired
    ObjectMapper objectMapper;

    @Setter
    @Autowired
    RedisRepo redisRepo;

    @Setter
    @Autowired
    private GraphReq graphOriginalReq;

    @Override
    public void process(Exchange exchange) throws Exception {
        if (switchOn && whiteList != null && whiteList.size() > 0) {
            for (String companyName : whiteList) {
                GraphReq req = (GraphReq) graphOriginalReq.clone();
                req.setCompany(companyName);

                String companyId = "Company/" + SecretUtil.md5(companyName);
                Graph graph = graphService.buildGraph(companyId, req, true);
                if (graph != null) {
                    redisRepo.pushGraphCache(companyId, graph);
                }
            }
        }
    }
}
