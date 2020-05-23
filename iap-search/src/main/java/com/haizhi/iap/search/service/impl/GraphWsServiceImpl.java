package com.haizhi.iap.search.service.impl;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.haizhi.iap.search.component.GraphWSHolder;
import com.haizhi.iap.search.controller.GraphWS;
import com.haizhi.iap.search.model.DcStore;
import com.haizhi.iap.search.repo.atlas.DcStoreRepo;
import com.haizhi.iap.search.service.GraphWsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * @author mtl
 * @Description:
 * @date 2020/4/2 17:23
 */
@Slf4j
@Service
public class GraphWsServiceImpl implements GraphWsService {

    @Autowired
    private GraphWSHolder holder;

    @Autowired
    private DcStoreRepo dcStoreRepo;

    @Qualifier("atlasGraphWS")
    @Autowired
    private GraphWS defaultGraphWS;

    @Value("${atlas.arango.switch.modelurl}")
    private String modelurl;

    /**
     * 切换GDB
     * @return 新的gdb地址
     */
    @Override
    public String switchGDB() {
        DcStore dcStore = dcStoreRepo.findDcStore("GDB");
        if(null == dcStore){
            holder.setWs(defaultGraphWS);
            return "default";
        }else{
            String url = modelurl.replace("@ip_port",dcStore.getUrl());
            String username = dcStore.getUser_name();
            String password = dcStore.getPassword();
            log.info("GDB switch to {}",url);
            GraphWS graphWS = JAXRSClientFactory
                    .create(url, GraphWS.class, Collections.singletonList(JacksonJsonProvider.class),username,password,null);
            WebClient.getConfig(graphWS).getHttpConduit().getClient().setAllowChunking(false);
            holder.setWs(graphWS);
            return url;
        }
    }

//    @Override
//    public Map currentGDB() {
//        return null;
//    }
}
