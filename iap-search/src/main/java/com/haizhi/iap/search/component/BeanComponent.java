package com.haizhi.iap.search.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.haizhi.iap.search.controller.model.GraphReq;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by chenbo on 2017/8/10.
 */
@Slf4j
@Component
public class BeanComponent {

    @Setter
    @Resource(name = "graphParamsInput")
    InputStream graphParamsInput;

    @Setter
    @Autowired
    ObjectMapper objectMapper;

    @Bean
    public GraphReq getGraphReq() throws IOException {
        return objectMapper.readValue(graphParamsInput, GraphReq.class);
    }
}
