package com.haizhi.iap.tag.service;

import com.haizhi.iap.tag.model.TagCollection;
import com.haizhi.iap.tag.model.TagCollectionExample;

import java.util.Map;

public interface TagCollectionService {

    int addTagCollection(TagCollection tagCollection);

    int updateTagCollection(TagCollection record, TagCollectionExample example);
}
