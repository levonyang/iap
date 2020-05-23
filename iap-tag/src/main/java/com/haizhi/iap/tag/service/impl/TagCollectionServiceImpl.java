package com.haizhi.iap.tag.service.impl;

import com.haizhi.iap.tag.dao.TagCollectionMapper;
import com.haizhi.iap.tag.model.TagCollection;
import com.haizhi.iap.tag.model.TagCollectionExample;
import com.haizhi.iap.tag.repo.ElasticSearchRepo;
import com.haizhi.iap.tag.service.TagCollectionService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.Inet4Address;
import java.util.Map;

@Slf4j
@Service
public class TagCollectionServiceImpl implements TagCollectionService {

    @Setter
    @Autowired
    TagCollectionMapper tagCollectionMapper;

    @Override
    public int updateTagCollection(TagCollection record, TagCollectionExample example) {
        return tagCollectionMapper.updateByExampleSelective(record, example);
    }

    @Override
    public int addTagCollection(TagCollection tagCollection) {
        return tagCollectionMapper.insert(tagCollection);
    }
}
