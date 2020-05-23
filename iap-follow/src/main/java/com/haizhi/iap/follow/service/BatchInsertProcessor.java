package com.haizhi.iap.follow.service;

import com.haizhi.iap.follow.model.FollowItem;
import com.haizhi.iap.follow.repo.FollowItemRepo;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by chenbo on 17/5/24.
 */
@Slf4j
@Service
public class BatchInsertProcessor implements Processor {
    @Setter
    @Autowired
    FollowItemRepo followItemRepo;

    private Integer batchSize = 500;

    @Override
    public void process(Exchange exchange) throws Exception {
        List<FollowItem> items = (List<FollowItem>) exchange.getIn().getBody();

        if(items == null){
            return;
        }
        if (items.size() <= batchSize) {
            followItemRepo.batchInsert(items);
        } else {
            int total = items.size() % batchSize == 0 ?
                    items.size() / batchSize : items.size() / batchSize + 1;
            for (int i = 0; i < total; i++) {
                int toIndex = (i + 1) * batchSize > items.size() ? items.size() : (i + 1) * batchSize;
                log.info("batch insert from {} to {}", i * batchSize, toIndex);
                followItemRepo.batchInsert(items.subList(i * batchSize, toIndex));
            }
        }
    }

}
