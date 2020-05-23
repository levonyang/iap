package com.haizhi.iap.follow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by chenbo on 17/5/27.
 */
@Slf4j
@Service
public class BatchCrawProcessor implements Processor {

    @Setter
    @Value("${ip.deep_search}")
    String crawlerUri;

    @Setter
    @Autowired
    ObjectMapper objectMapper;

    @Setter
    @Autowired
    ClientConnectionPool clientConnectionPool;

    @Setter
    @Value(value = "${crawl.switch}")
    Boolean switchOn;

    private Integer batchSize = 500;

    @Override
    public void process(Exchange exchange) throws Exception {
        if(!switchOn){
            log.warn("crawl switch off, no need to access crawl api.");
            return;
        }

        List<Map> crawData = (List<Map>) exchange.getIn().getBody();

        if (crawData == null) {
            return;
        }
        try {
            URIBuilder builder;

            builder = new URIBuilder(crawlerUri + "/api/updatecompanydata");

            HttpPost post = new HttpPost(builder.build());

            if (crawData.size() <= batchSize) {
                log.info("request crawler {}", crawData);
                post.setEntity(new StringEntity(
                        objectMapper.writeValueAsString(Collections.singletonMap("data", crawData)),
                        ContentType.APPLICATION_JSON));
                String result = clientConnectionPool.execute(post);
                log.info(result);
            } else {
                int total = crawData.size() % batchSize == 0 ?
                        crawData.size() / batchSize : crawData.size() / batchSize + 1;
                for (int i = 0; i < total; i++) {
                    int toIndex = (i + 1) * batchSize > crawData.size() ? crawData.size() : (i + 1) * batchSize;
                    log.info("request crawler from {} to {}", i * batchSize, toIndex);
                    post.setEntity(new StringEntity(
                            objectMapper.writeValueAsString(Collections.singletonMap("data", crawData.subList(i * batchSize, toIndex))),
                            ContentType.APPLICATION_JSON));
                    String result = clientConnectionPool.execute(post);
                    log.info(result);
                }
            }

        } catch (Exception e) {
            log.error("{}", e);
        }
    }
}
