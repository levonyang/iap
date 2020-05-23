package com.haizhi.iap.tag.component;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by chenbo on 17/6/20.
 */
@Slf4j
@Component
public class ESComponent {

    @Setter
    @Value("${es.hosts}")
    private String hosts;

    @Setter
    @Value("${es.cluster.name}")
    private String clusterName;

    private static final int DEFAULT_ES_PORT = 9300;

    @Bean
    public Client getESClient() {
        if (Strings.isNullOrEmpty(hosts)) {
            throw new IllegalArgumentException("elasticsearch hosts can not be null or empty!");
        } else {
            log.info("elasticsearch hosts: {}", hosts);
        }

        if (Strings.isNullOrEmpty(clusterName)) {
            throw new IllegalArgumentException("elasticsearch cluster.name can not be null or empty!");
        } else {
            log.info("elasticsearch cluster.name: {}", clusterName);
        }

        Settings settings = Settings.builder()
                .put("cluster.name", clusterName)
                .build();
        TransportClient client = new PreBuiltTransportClient(settings);

        for (String address : String.valueOf(hosts).split(",")) {
            if (address != null && !address.equals("")) {
                try {
                    client.addTransportAddress(
                            new TransportAddress(
                                    InetAddress.getByName(address.split(":")[0].trim()),
                                    Integer.valueOf(address.split(":")[1].trim())));
                } catch (UnknownHostException e) {
                    log.error("{}", e);
                }
            }
        }
        return client;
    }
}
