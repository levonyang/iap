package com.haizhi.iap.mobile.component;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import lombok.Setter;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * Created by chenbo on 17/6/20.
 */
@Component
public class WSComponent
{

    @Setter
    @Value("${ip.graph}")
    String graphIP;

    @Setter
    @Value("${arango.username}")
    String graphUserName;

    @Setter
    @Value("${arango.password}")
    String graphPassWord;

    @Bean
    public GraphWS getGraphWS() {

        //return JAXRSClientFactory.create(graphIP, GraphWS.class, Collections.singletonList(JacksonJsonProvider.class));
        return JAXRSClientFactory.create(graphIP, GraphWS.class, Collections.singletonList(JacksonJsonProvider.class),graphUserName,graphPassWord,null);
    }
}
