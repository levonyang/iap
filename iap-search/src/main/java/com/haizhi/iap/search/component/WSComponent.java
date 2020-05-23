package com.haizhi.iap.search.component;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.haizhi.iap.search.controller.GraphFoxxWS;
import com.haizhi.iap.search.controller.GraphWS;
import lombok.Setter;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * Created by chenbo on 17/6/20.
 */
@Component
public class WSComponent {

    @Setter
    @Value("${ip.graph}")
    String graphIP;

    @Setter
    @Value("${arango.username}")
    String graphUserName;

    @Setter
    @Value("${arango.password}")
    String graphPassWord;

    @Setter
    @Value("${atlas.ip.graph}")
    String atlasGraphIP;

    @Setter
    @Value("${atlas.arango.username}")
    String atlasGraphUserName;

    @Setter
    @Value("${atlas.arango.password}")
    String atlasGraphPassWord;

    @Bean
    public GraphFoxxWS getGraphFoxxWS() {
        //GraphFoxxWS foxxWS = JAXRSClientFactory.create(graphIP, GraphFoxxWS.class, Collections.singletonList(JacksonJsonProvider.class));
        GraphFoxxWS foxxWS = JAXRSClientFactory.create(graphIP,GraphFoxxWS.class, Collections.singletonList(JacksonJsonProvider.class),graphUserName,graphPassWord, null);
        //
//        WebClient.getConfig(foxxWS).setInInterceptors(Collections.singletonList(new LoggingInInterceptor()));
//        WebClient.getConfig(foxxWS).setOutInterceptors(Collections.singletonList(new LoggingOutInterceptor()));

        return foxxWS;
    }

    /**
     * 老图谱arango查询
     * @return
     */
    @Primary
    @Bean
    public GraphWS getGraphWS() {
        //GraphWS graphWS = JAXRSClientFactory.create(graphIP, GraphWS.class, Collections.singletonList(JacksonJsonProvider.class));
        GraphWS graphWS = JAXRSClientFactory.create(graphIP, GraphWS.class, Collections.singletonList(JacksonJsonProvider.class),graphUserName,graphPassWord,null);
        WebClient.getConfig(graphWS).getHttpConduit().getClient().setAllowChunking(false);

        return graphWS;
    }

    /**
     * 图平台arango(默认节点)查询,如果从数据库获取当前节点失败，就使用默认节点进行查询
     * @return
     */
    @Bean
    public GraphWS atlasGraphWS() {
        GraphWS graphWS = JAXRSClientFactory
                .create(atlasGraphIP, GraphWS.class, Collections.singletonList(JacksonJsonProvider.class),atlasGraphUserName,atlasGraphPassWord,null);
        WebClient.getConfig(graphWS).getHttpConduit().getClient().setAllowChunking(false);
        return graphWS;
    }

    @Bean
    public GraphWSHolder graphWSHolder(){
        return new GraphWSHolder(atlasGraphWS());
    }

}
