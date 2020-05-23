package com.haizhi.iap.search.service;

import com.haizhi.iap.search.controller.model2.SearchRequest;

/**
 * Created by chenbo on 2017/11/7.
 */
public interface CompanyService {

    Object search(SearchRequest request);

    Object searchV3(SearchRequest request);

    boolean isExists(String companyName);

}
