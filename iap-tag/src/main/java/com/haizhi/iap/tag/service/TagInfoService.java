package com.haizhi.iap.tag.service;

import com.haizhi.iap.tag.controller.param.TagParticipleParam;
import com.haizhi.iap.tag.model.TagDetail;
import com.haizhi.iap.tag.model.TagInfo;
import com.haizhi.iap.tag.recognizer.RecognizerManager;

import java.util.List;

/**
 * @Author dmy
 * @Date 2017/11/2 下午8:55.
 */
public interface TagInfoService {

    /**
     * 构建标签字典
     */
    void buildTagDictionary();

    /**
     * 从tagParticipleParam.content中进行关键词解析，获取库中对应的标签
     * @param tagParticipleParam
     * @return
     */
    List<TagInfo> findTagFromContent(TagParticipleParam tagParticipleParam);

    /**
     * 通过id获取标签信息
     * @param id
     * @return
     */
    TagDetail getInfoById(Integer id);

    /**
     * 通过标签id获取父级信息
     * @param id
     * @return
     */
    TagDetail getParentInfoById(Integer id);

    /**
     * 通过标签id获取所有子级id
     * @param id
     * @return
     */
    List<Integer> getChildsInfoById(Integer id);

}
