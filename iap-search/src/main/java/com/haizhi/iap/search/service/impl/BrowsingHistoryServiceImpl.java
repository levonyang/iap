package com.haizhi.iap.search.service.impl;

import com.haizhi.iap.search.model.BrowsingHistory;
import com.haizhi.iap.search.repo.BrowsingHistoryRepo;
import com.haizhi.iap.search.service.BrowsingHistoryService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by chenbo on 2017/11/8.
 */
@Service
public class BrowsingHistoryServiceImpl implements BrowsingHistoryService {
    @Setter
    @Autowired
    BrowsingHistoryRepo browsingHistoryRepo;

    @Override
    public List<BrowsingHistory> findByUser(Long userId, Integer offset, Integer count) {
        return browsingHistoryRepo.findByUser(userId, offset, count);
    }

    @Override
    public Long countByUser(Long userId) {
        return browsingHistoryRepo.countByUser(userId);
    }
}
