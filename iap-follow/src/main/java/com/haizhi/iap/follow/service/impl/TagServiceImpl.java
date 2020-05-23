package com.haizhi.iap.follow.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.haizhi.iap.follow.controller.model.TagFamily;
import com.haizhi.iap.follow.model.Tag;
import com.haizhi.iap.follow.model.TagLevelOneType;
import com.haizhi.iap.follow.repo.TagRepo;
import com.haizhi.iap.follow.service.TagService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by chenbo on 2017/12/14.
 */
@Service
public class TagServiceImpl implements TagService {

    @Setter
    @Autowired
    TagRepo tagRepo;

    @Override
    public Map<String, List<Tag>> getHot(TagLevelOneType levelOneType, String keyword, Integer count) {
        Map<String, List<Tag>> result = Maps.newHashMap();
        switch (levelOneType) {
            case ALL:
                result.put(TagLevelOneType.ADMIN_REGION.getName(), getArea(keyword, true, count));
                result.put(TagLevelOneType.INDUSTRY.getName(), getIndustry(keyword, true, count));
                result.put(TagLevelOneType.PRODUCT.getName(), getProduct(keyword, true, count));
                result.put(TagLevelOneType.HOT_WORDS.getName(), getStock(keyword, true, count));
                break;
            case ADMIN_REGION:
                result.put(TagLevelOneType.ADMIN_REGION.getName(), getArea(keyword, true, count));
                break;
            case INDUSTRY:
                result.put(TagLevelOneType.INDUSTRY.getName(), getIndustry(keyword, true, count));
                break;
            case PRODUCT:
                result.put(TagLevelOneType.PRODUCT.getName(), getProduct(keyword, true, count));
                break;
            case HOT_WORDS:
                //热门主题对应股票热点
                result.put(TagLevelOneType.HOT_WORDS.getName(), getStock(keyword, true, count));
                break;
        }
        return result;
    }

    @Override
    public Map<String, List<Tag>> search(TagLevelOneType levelOneType, String keyword, Integer count) {
        Map<String, List<Tag>> result = Maps.newHashMap();
        switch (levelOneType) {
            case ALL:
                result.put(TagLevelOneType.ADMIN_REGION.getName(), getArea(keyword, null, count));
                result.put(TagLevelOneType.INDUSTRY.getName(), getIndustry(keyword, null, count));
                result.put(TagLevelOneType.PRODUCT.getName(), getProduct(keyword, null, count));
                result.put(TagLevelOneType.HOT_WORDS.getName(), getStock(keyword, null, count));
                break;
            case ADMIN_REGION:
                result.put(TagLevelOneType.ADMIN_REGION.getName(), getArea(keyword, null, count));
                break;
            case INDUSTRY:
                result.put(TagLevelOneType.INDUSTRY.getName(), getIndustry(keyword, null, count));
                break;
            case PRODUCT:
                result.put(TagLevelOneType.PRODUCT.getName(), getProduct(keyword, null, count));
                break;
            case HOT_WORDS:
                //热门主题对应股票热点
                result.put(TagLevelOneType.HOT_WORDS.getName(), getStock(keyword, null, count));
                break;
        }
        return result;
    }

    @Override
    public List<TagFamily> getAllTag(TagLevelOneType levelOneType) {
        List<TagFamily> result = null;
        switch (levelOneType) {
            case ADMIN_REGION:
                result = getAllRegion();
                break;
            case INDUSTRY:
                result = getAllIndustry();
                break;
        }
        return result;
    }

    public List<Tag> getArea(String keyword, Boolean isHot, Integer count) {
        return tagRepo.getArea(keyword, isHot, 0, count);
    }

    public List<Tag> getIndustry(String keyword, Boolean isHot, Integer count) {
        return tagRepo.getIndustry(keyword, isHot, 0, count);
    }

    public List<Tag> getProduct(String keyword, Boolean isHot, Integer count) {
        return tagRepo.getProduct(keyword, isHot, 0, count);
    }

    public List<Tag> getHot(String keyword, Integer count) {
        return tagRepo.findByCondition(null, keyword, true, 0, count);
    }

    private List<Tag> getStock(String keyword, Boolean isHot, Integer count) {
        return tagRepo.getStock(keyword, isHot, 0, count);
    }

    public List<TagFamily> getAllRegion() {
        List<Tag> allRegionTag = tagRepo.getArea(null, null, null, null);
        return flat(allRegionTag);
    }

    public List<TagFamily> getAllIndustry() {
        List<Tag> allIndustryTag = tagRepo.getIndustry(null, null, null, null);
        return flat(allIndustryTag);
    }

    public List<TagFamily> flat(List<Tag> tagList) {
        List<TagFamily> result = Lists.newArrayList();
        Map<Long, TagFamily> pIdFamilyMap = Maps.newHashMap();
        for (Tag tag : tagList) {
            if (tag.getLevel() == 1) {
                TagFamily family = new TagFamily();
                family.setParent(tag);
                family.setChildren(Lists.newArrayList());
                pIdFamilyMap.put(tag.getId(), family);
            }
        }
        for (Tag tag : tagList) {
            if (tag.getLevel() == 2) {
                TagFamily family = pIdFamilyMap.get(tag.getParentId());
                if (family != null) {
                    List<Tag> children = family.getChildren();
                    children.add(tag);
                }
            }
        }
        result.addAll(pIdFamilyMap.values());
        if (result.size() > 0) {
            Collections.sort(result);
        }
        return result;
    }
}
