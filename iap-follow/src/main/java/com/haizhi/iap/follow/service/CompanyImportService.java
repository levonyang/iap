package com.haizhi.iap.follow.service;

import com.haizhi.iap.follow.controller.model.CompanyImportAck;

/**
 * Created by chenbo on 17/1/9.
 */
public interface CompanyImportService {

    CompanyImportAck importFromTXT(byte[] data);

    CompanyImportAck importFromExcel(byte[] data);

    void commit(String cacheKey, Long followListId, Long userId);
}
