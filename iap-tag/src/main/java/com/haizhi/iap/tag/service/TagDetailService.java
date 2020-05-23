package com.haizhi.iap.tag.service;

import com.haizhi.iap.tag.model.TagDetail;
import com.haizhi.iap.tag.model.TagDetailExample;

import java.util.List;

public interface TagDetailService {

    /**
     * @param tagDetail
     * @return
     * @Description: 新增一个标签
     * @date 2017年10月25日 下午5:01:01
     */
    public int addTag(TagDetail tagDetail);

    /**
     * @param record,example
     * @return
     * @Description: 更新一个标签
     * @date 2017年10月25日 下午5:01:26
     */
    public int updateTag(TagDetail record, TagDetailExample example);

    /**
     * @param tagId
     * @return
     * @Description: 删除一个标签
     * @date 2017年10月25日 下午5:01:59
     */
    public int deleteTag(int tagId);

    /**
     * @param tagCollectionId
     * @return
     * @Description: 查询一个标签集合
     * @date 2017年10月25日 下午5:02:15
     */
    public List<TagDetail> getTagList(int tagCollectionId);

    /**
     * @param tagCollectionId
     * @return List<TagDetail>
     * @time 11:08
     * @method getTagsByCollectionId
     * @description 根据集合ID查询标签集合
     */
    public List<TagDetail> getTagsByCollectionId(Integer tagCollectionId);

    /**
     * 通过curTag(当前标签)获取(parentLevel~curLevel-1)级父标签
     * @param curTag
     * @param parentLevel
     * @return
     */
    public List<TagDetail> getParentTags(TagDetail curTag, int parentLevel);

}
