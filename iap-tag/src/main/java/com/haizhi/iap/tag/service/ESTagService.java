package com.haizhi.iap.tag.service;

import com.haizhi.iap.tag.model.SearchTagResponse;
import com.haizhi.iap.tag.param.MapDataRequest;
import com.haizhi.iap.tag.param.TagDetailRequest;
import com.haizhi.iap.tag.param.TagDetailSearchRequest;

import java.util.List;
import java.util.Map;

public interface ESTagService {

    boolean addTagCollection(String collectionName, Map<String, Object> clientParam);

    SearchTagResponse searchES(TagDetailSearchRequest tagDetailSearchRequest);

    SearchTagResponse searchWithParent(TagDetailSearchRequest tagDetailSearchRequest);

    Integer bulkImportData(MapDataRequest mapDataRequest);
}
