package com.haizhi.iap.follow.service;

import com.haizhi.iap.follow.controller.model.TagFamily;
import com.haizhi.iap.follow.model.Tag;
import com.haizhi.iap.follow.model.TagLevelOneType;

import java.util.List;
import java.util.Map;

/**
 * Created by chenbo on 2017/12/14.
 */
public interface TagService {

    Map<String, List<Tag>> getHot(TagLevelOneType levelOneType, String keyword, Integer count);

    Map<String, List<Tag>> search(TagLevelOneType levelOneType, String keyword, Integer count);

    List<TagFamily> getAllTag(TagLevelOneType levelOneType);

}
