package com.haizhi.iap.search.component;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenbo on 17/6/20.
 */
@Slf4j
@Component
public class ESComponent {

    private static final int DEFAULT_ES_PORT = 9300;

    @Setter
    @Value("${es.hosts}")
    private String hosts;

    @Setter
    @Value("${es.cluster.name}")
    private String clusterName;

    @Bean
    public Client getESClient() {
//        if (Strings.isNullOrEmpty(hosts)) {
//            throw new IllegalArgumentException("elasticsearch hosts can not be null or empty!");
//        } else {
//            log.info("elasticsearch hosts: {}", hosts);
//        }
//
//        if (Strings.isNullOrEmpty(clusterName)) {
//            throw new IllegalArgumentException("elasticsearch cluster.name can not be null or empty!");
//        } else {
//            log.info("elasticsearch cluster.name: {}", clusterName);
//        }
//
//        Settings settings = Settings.builder()
//                .put("cluster.name", clusterName)
//                .build();
//        TransportClient client = new PreBuiltTransportClient(settings);
//
//        for (String address : String.valueOf(hosts).split(",")) {
//            if (address != null && !address.equals("")) {
//                try {
//                    client.addTransportAddress(
//                            new TransportAddress(
//                                    InetAddress.getByName(address.split(":")[0].trim()),
//                                    Integer.valueOf(address.split(":")[1].trim())));
//                } catch (UnknownHostException e) {
//                    log.error("{}", e);
//                }
//            }
//        }
//        return client;
        return null;
    }

    @Bean
    public RestClientBuilder restClientBuilder(){
        String[] hostArr = String.valueOf(hosts).split(",");
        List<HttpHost> hostList = new ArrayList<>();
        for (String address : hostArr) {
            if (address != null && !address.equals("")) {
                String[] addArr = address.split(":");
                String host = addArr[0];
                int port = DEFAULT_ES_PORT;
                if(addArr.length > 1){
                    port = Integer.valueOf(addArr[1].trim());
                }
                HttpHost httpHost = new HttpHost(addArr[0],port,"http");
                hostList.add(httpHost);
            }
        }
        RestClientBuilder builder = RestClient.builder((HttpHost[]) hostList.toArray());
        return builder;
    }

    @Bean
    public RestHighLevelClient restHighLevelClient(){
        return new RestHighLevelClient(restClientBuilder());
    }

    @Bean
    public RestClient restClient(){
        return restClientBuilder().build();
    }

}
