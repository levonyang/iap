package com.haizhi.iap.follow.repo;

import com.google.common.collect.Maps;
import com.haizhi.iap.common.exception.ServiceAccessException;
import lombok.Setter;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * Created by chenbo on 2017/7/17.
 */
@Repository
public class ElasticSearchRepo {

    @Setter
    @Autowired
    Client client;

    public Map<String, Object> getById(String index, String type, String id) {
        try {
            Map<String, Object> result = Maps.newHashMap();
            GetResponse response = client.prepareGet(index,
                    type, id).execute().get();
            if (response.isExists()) {
                result = response.getSourceAsMap();
            }
            return result;
        } catch (Exception e) {
            throw new ServiceAccessException(-1, e.getMessage());
        }
    }

}
