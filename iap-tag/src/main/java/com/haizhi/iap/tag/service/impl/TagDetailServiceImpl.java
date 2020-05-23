package com.haizhi.iap.tag.service.impl;

import com.google.common.collect.Lists;
import com.haizhi.iap.common.exception.ServiceAccessException;
import com.haizhi.iap.tag.dao.TagDetailMapper;
import com.haizhi.iap.tag.exception.TagException;
import com.haizhi.iap.tag.model.TagDetail;
import com.haizhi.iap.tag.model.TagDetailExample;
import com.haizhi.iap.tag.repo.TagRepo;
import com.haizhi.iap.tag.service.TagDetailService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class TagDetailServiceImpl implements TagDetailService {

    @Setter
    @Autowired
    TagDetailMapper tagDetailMapper;

    @Setter
    @Autowired
    TagRepo tagRepo;

    @Override
    public int addTag(TagDetail tagDetail) {
        return tagDetailMapper.insert(tagDetail);
    }

    @Override
    public int updateTag(TagDetail record, TagDetailExample example) {
        return tagDetailMapper.updateByExampleSelective(record, example);
    }

    @Override
    public int deleteTag(int tagId) {
        TagDetailExample example = new TagDetailExample();
        TagDetailExample.Criteria criteria = example.createCriteria();
        criteria.andIdEqualTo(tagId);
        return tagDetailMapper.deleteByExample(example);
    }

    @Override
    public List<TagDetail> getTagList(int tagCollectionId) {
        TagDetailExample texample = new TagDetailExample();
        TagDetailExample.Criteria criteria = texample.createCriteria();
        criteria.andParentIdEqualTo(tagCollectionId);
        return tagDetailMapper.selectByExample(texample);
    }


    @Override
    public List<TagDetail> getTagsByCollectionId(Integer tagCollectionId) {
        TagDetailExample tagDetailExample = new TagDetailExample();
        TagDetailExample.Criteria criteria = tagDetailExample.createCriteria();
        criteria.andCollectionIdEqualTo(tagCollectionId);
        return tagDetailMapper.selectByExample(tagDetailExample);
    }

    @Override
    public List<TagDetail> getParentTags(TagDetail curTag, int parentLevel) {
        List<TagDetail> tagDetails = Lists.newArrayList();
        TagDetail tagDetail = null;

        for(int level = curTag.getLevel()-1; level >= parentLevel; level--) {
            tagDetail = tagRepo.findById(curTag.getParentId());
            if(tagDetail != null) {
                tagDetails.add(tagDetail);
                curTag = tagDetail;
            } else {
                throw new ServiceAccessException(TagException.PARENT_TAG_NOT_EXIST);
            }
        }
        Collections.reverse(tagDetails);
        return tagDetails;
    }
}
