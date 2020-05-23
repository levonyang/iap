package com.haizhi.iap.tag.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.haizhi.iap.tag.component.TaskProcessor;
import com.haizhi.iap.tag.controller.param.TagParticipleParam;
import com.haizhi.iap.tag.enums.TagType;
import com.haizhi.iap.tag.model.TagDetail;
import com.haizhi.iap.tag.model.TagInfo;
import com.haizhi.iap.tag.recognizer.RecognizerManager;
import com.haizhi.iap.tag.recognizer.meta.AbstractDictRecord;
import com.haizhi.iap.tag.recognizer.meta.Entity;
import com.haizhi.iap.tag.recognizer.meta.PatternRecord;
import com.haizhi.iap.tag.recognizer.meta.RecordType;
import com.haizhi.iap.tag.repo.RedisRepo;
import com.haizhi.iap.tag.repo.TagRepo;
import com.haizhi.iap.tag.service.TagDetailService;
import com.haizhi.iap.tag.service.TagInfoService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author dmy
 * @Date 2017/11/2 下午8:55.
 */
@Service
@Slf4j
public class TagInfoServiceImpl implements TagInfoService {

    @Setter
    @Autowired
    TagRepo tagRepo;

    @Setter
    @Autowired
    TagDetailService tagDetailService;

    @Setter
    @Autowired
    RedisRepo redisRepo;

    public RecognizerManager recognizerManager = new RecognizerManager();

    private Map<Integer, TagDetail> tagInfoMap = Maps.newHashMap();
    private Map<Integer, Integer> tagParentInfoMap = Maps.newHashMap();
    private Map<Integer, List<Integer>> tagChildInfoMap = Maps.newHashMap();

    @Override
    public void buildTagDictionary() {
        //从数据库读取标签，构建字典
        List<TagDetail> tagDatas = tagRepo.findAll();
        dealTagDatas(tagDatas);

        List<AbstractDictRecord> data = new ArrayList<>();
        AbstractDictRecord dictRecord = null;

        for (TagDetail tagDetail : tagDatas) {
            dictRecord = new PatternRecord(
                    new RecordType(0, tagDetail.getParentId(), "default"),
                    tagDetail.getName(), tagDetail);
            data.add(dictRecord);
        }

        RecognizerManager recognizerManagerNew = new RecognizerManager();
        recognizerManagerNew.buildDictionary(data);
        recognizerManager = recognizerManagerNew;
    }

    @Override
    public List<TagInfo> findTagFromContent(TagParticipleParam tagParticipleParam) {
        List<TagInfo> tagInfos = Lists.newArrayList();

        //进行关键词解析
        List<Entity> all = recognizerManager.recognizeEntities(tagParticipleParam.getContent());
        TagDetail tagDetail = null;

        for (Entity one : all) {
            tagDetail = (TagDetail) ((PatternRecord) one.getRecord()).getData();
            tagInfos.add(dealByParam(tagParticipleParam, tagDetail, one));
        }

        return tagInfos;
    }

    @Override
    public TagDetail getInfoById(Integer id) {
        return tagInfoMap.get(id);
    }

    @Override
    public TagDetail getParentInfoById(Integer id) {
        return tagInfoMap.get(tagParentInfoMap.get(id));
    }

    @Override
    public List<Integer> getChildsInfoById(Integer id) {
        return tagChildInfoMap.get(id);
    }

    private TagInfo dealByParam(TagParticipleParam tagParticipleParam, TagDetail tagDetail, Entity entity) {
        TagInfo tagInfo = TagInfo.builder().build();

        if (tagParticipleParam.isR_tag_id())
            tagInfo.setId(tagDetail.getId());
        if (tagParticipleParam.isR_start_index())
            tagInfo.setStart(entity.getStart());
        if (tagParticipleParam.isR_end_index())
            tagInfo.setEnd(entity.getEnd());
        if (tagParticipleParam.isR_tag_name())
            tagInfo.setName(tagDetail.getName().trim());
        if (tagParticipleParam.isR_tag_type())
            tagInfo.setTagType(TagType.values()[tagDetail.getTagType()].name());
        if (tagParticipleParam.isR_tag_fname())
            tagInfo.setFieldName(tagDetail.getFieldName());

        try {
            if (tagParticipleParam.isR_tag_pids())
                tagInfo.setParentIds(getParentInfos(tagDetail, 1, "pid", TagDetail.class.getMethod("getId")));
            if (tagParticipleParam.isR_tag_pnames())
                tagInfo.setParentNames(getParentInfos(tagDetail, 1, "pname", TagDetail.class.getMethod("getName")));
            if (tagParticipleParam.isR_tag_pfnames())
                tagInfo.setParentFieldNames(getParentInfos(tagDetail, 1, "pfname", TagDetail.class.getMethod("getFieldName")));
        } catch (NoSuchMethodException ne) {
            log.error("{}", ne);
        }

        return tagInfo;
    }

    private <T> List<T> getParentInfos(TagDetail curTag, int parentLevel, String key, Method method) {
        String pKey = StringUtils.joinWith(":", curTag.getId(), key, parentLevel);
        List<T> parentInfos = redisRepo.getTagParentInfos(pKey);

        if (parentInfos == null) {
            try {
                parentInfos = Lists.newArrayList();
                List<TagDetail> tagDetails = tagDetailService.getParentTags(curTag, parentLevel);
                for (TagDetail td : tagDetails) {
                    parentInfos.add((T) method.invoke(td));
                }
            } catch (Exception e) {
                log.error("{}", e);
            }

            redisRepo.pushTagParentInfos(pKey, parentInfos);
        }

        return parentInfos;
    }

    private void dealTagDatas(List<TagDetail> tagDatas) {
        Map<Integer, TagDetail> tagInfoNewMap = Maps.newHashMap();
        Map<Integer, Integer> tagParentInfoNewMap = Maps.newHashMap();
        Map<Integer, List<Integer>> tagChildInfoNewMap = Maps.newHashMap();

        tagDatas.stream().forEach(tagDetail -> {
            tagInfoNewMap.put(tagDetail.getId(), tagDetail);
            tagParentInfoNewMap.put(tagDetail.getId(), tagDetail.getParentId());

            if(tagChildInfoNewMap.containsKey(tagDetail.getParentId())) {
                List<Integer> childIds = tagChildInfoNewMap.get(tagDetail.getParentId());
                childIds.add(tagDetail.getId());
                tagChildInfoNewMap.put(tagDetail.getParentId(), childIds);
            }
        });

        tagInfoMap = tagInfoNewMap;
        tagParentInfoMap = tagParentInfoNewMap;
        tagChildInfoMap = tagChildInfoNewMap;
    }

}
