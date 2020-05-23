package com.haizhi.iap.proxy.component;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.haizhi.iap.proxy.controller.DeepSearchWS;
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
public class WSComponent {

    @Setter
    @Value("${ip.deep_search}")
    String crawlIP;

    @Bean
    public DeepSearchWS getDeepSearchWS() {
        return JAXRSClientFactory.create(crawlIP, DeepSearchWS.class, Collections.singletonList(JacksonJsonProvider.class));
    }
}
