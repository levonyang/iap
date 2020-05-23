package com.haizhi.iap.search.service;

import com.haizhi.iap.search.model.BrowsingHistory;

import java.util.List;

/**
 * Created by chenbo on 2017/11/8.
 */
public interface BrowsingHistoryService {
    List<BrowsingHistory> findByUser(Long userId, Integer offset, Integer count);

    Long countByUser(Long userId);
}
