package com.haizhi.iap.search.component;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.haizhi.iap.search.controller.GraphWS;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.WebClient;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author mtl
 * @Description:
 * @date 2020/4/2 16:01
 */
public class GraphWSHolder {

    private static final int RETRY = 3; //最多重试3次
    private static final long WAIT_SECONDS = 3; //等待3秒
    private AtomicReference<GraphWS> reference;

    public GraphWSHolder() {
        this.reference = new AtomicReference<>();
    }

    public GraphWSHolder(GraphWS graphWS) {
        this.reference = new AtomicReference<>();
        this.reference.set(graphWS);
    }

    public void setWs(GraphWS ws){
        this.reference.set(ws);
    }

    public void setWs(String url,String username,String password){
        GraphWS graphWS = JAXRSClientFactory
                .create(url, GraphWS.class, Collections.singletonList(JacksonJsonProvider.class),username,password,null);
        WebClient.getConfig(graphWS).getHttpConduit().getClient().setAllowChunking(false);
        this.reference.set(graphWS);
    }

    public GraphWS getWs() throws Exception {
        int i = 1;
        while(i <= RETRY){
            GraphWS ws = this.reference.get();
            if(null != ws){
                return ws;
            }
            i++;
            TimeUnit.SECONDS.sleep(WAIT_SECONDS);
        }
        throw new RuntimeException("get GraphWs timeout");
    }
}
