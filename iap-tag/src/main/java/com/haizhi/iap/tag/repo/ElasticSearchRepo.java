package com.haizhi.iap.tag.repo;

import com.google.common.collect.Maps;
import com.haizhi.iap.tag.param.MapDataRequest;
import com.haizhi.iap.tag.utils.ElasticSearchConfig;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Slf4j
@Repository
public class ElasticSearchRepo {

    @Autowired
    ElasticSearchConfig elasticSearchConfig;

    @Autowired
    Client client;

    public boolean createCollection(String collectionName) {
        return createCollection(collectionName, elasticSearchConfig.DEFAULT_SETTINGS, elasticSearchConfig.DEFAULT_MAPPINGS);
    }

    public boolean createCollection(String collectionName, int shards, int replicas) {
        Map<String, Object> settings = Maps.newHashMap();
        settings.put("number_of_shards", shards);
        settings.put("number_of_replicas", replicas);
        return createCollection(collectionName, settings, elasticSearchConfig.DEFAULT_MAPPINGS);
    }

    public boolean createCollection(String collectionName, Map<String, Object> settings, Map<String, Object> mappings) {
        log.info("mappings: {}", mappings);
        CreateIndexResponse response = client.admin().indices().prepareCreate(collectionName)
                .setSettings(settings).addMapping(collectionName, mappings).get();
        return response.isAcknowledged();
    }

    public Integer bulkUpsertData(MapDataRequest mapDataRequest) {
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        for (Map<String, Object> data : mapDataRequest.getDatalist()) {
            if (data.get("_id") == null) {
                continue;
            }
            String id = (String) data.get("_id");
            data.remove("_id");
            bulkRequest.add(client.prepareUpdate(mapDataRequest.getEsIndexName(), mapDataRequest.getEsIndexName(), id)
            .setDoc(data).setDocAsUpsert(true));
        }
        BulkResponse bulkResponse = bulkRequest.get();
        if (bulkResponse.hasFailures()) {
            log.error("{}", bulkResponse.buildFailureMessage());
            return -1;
        }
        return bulkResponse.getItems().length;
    }
}
