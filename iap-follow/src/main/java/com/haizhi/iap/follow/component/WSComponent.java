package com.haizhi.iap.follow.component;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.haizhi.iap.follow.controller.InnerTagSearchWS;
import com.haizhi.iap.follow.controller.InternalSearchWS;
import com.haizhi.iap.follow.controller.NotificationWS;
import lombok.Setter;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transport.Conduit;
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
    @Value("${ip.search}")
    String searchIp;

    @Setter
    @Value("${ip.tag.search}")
    String tagSearchIp;

    @Setter
    @Value("${ip.notification}")
    String notificationIp;

    @Bean
    public InternalSearchWS getSearchWS() {
        InternalSearchWS internalSearchWS = JAXRSClientFactory.create(searchIp, InternalSearchWS.class, Collections.singletonList(JacksonJsonProvider.class));
//        WebClient.getConfig(internalSearchWS).setInInterceptors(Collections.singletonList(new LoggingInInterceptor()));
        WebClient.getConfig(internalSearchWS).setOutInterceptors(Collections.singletonList(new LoggingOutInterceptor()));
        return internalSearchWS;
    }

    @Bean
    public InnerTagSearchWS getTagSearchWS() {
        InnerTagSearchWS innerTagSearchWS = JAXRSClientFactory.create(tagSearchIp, InnerTagSearchWS.class, Collections.singletonList(JacksonJsonProvider.class));
        WebClient.getConfig(innerTagSearchWS).setOutInterceptors(Collections.singletonList(new LoggingOutInterceptor()));
        return innerTagSearchWS;
    }

    @Bean
    public NotificationWS getNotificationWS() {
        NotificationWS notificationWS = JAXRSClientFactory.create(notificationIp, NotificationWS.class, Collections.singletonList(JacksonJsonProvider.class));
        WebClient.getConfig(notificationWS).setOutInterceptors(Collections.singletonList(new LoggingOutInterceptor()));
        return notificationWS;
    }

}
