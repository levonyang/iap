package com.haizhi.iap.tag.param;

import java.util.Map;

public class CreateESCollectionRequest {
    String collectionName;
    Map<String, Object> param;

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public Map<String, Object> getParam() {
        return param;
    }

    public void setParam(Map<String, Object> param) {
        this.param = param;
    }
}
