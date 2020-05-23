package com.haizhi.iap.mobile.component;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Created by thomas on 18/4/11.
 */
@Component
public class RestComponent
{
    @Setter
    @Value("${rest.connect.timeout}")
    private int connectTimeout;

    @Bean
    public RestTemplate restTemplate()
    {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(connectTimeout);
        return new RestTemplate(requestFactory);
    }
}
