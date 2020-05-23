package com.haizhi.iap.tag.controller;

import com.google.common.collect.Maps;
import com.haizhi.iap.common.Wrapper;
import com.haizhi.iap.common.auth.NoneAuthorization;
import com.haizhi.iap.tag.model.SearchTagResponse;
import com.haizhi.iap.tag.model.TagCollection;
import com.haizhi.iap.tag.model.TagCollectionExample;
import com.haizhi.iap.tag.model.TagDetail;
import com.haizhi.iap.tag.model.TagDetailExample;
import com.haizhi.iap.tag.param.CreateESCollectionRequest;
import com.haizhi.iap.tag.param.MapDataRequest;
import com.haizhi.iap.tag.param.TagCollectionRequest;
import com.haizhi.iap.tag.param.TagDetailAddRequest;
import com.haizhi.iap.tag.param.TagDetailSearchRequest;
import com.haizhi.iap.tag.service.ESTagService;
import com.haizhi.iap.tag.service.TagCollectionService;
import com.haizhi.iap.tag.service.TagDetailService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.core.MediaType;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;


@Api(tags="【标签-ES标签模块】ES标签操作")
@Slf4j
@RestController
@RequestMapping(value = "/engine")
public class EsTagController {

    @Autowired
    private ESTagService esTagService;

    @Autowired
    private TagCollectionService tagCollectionService;

    @Autowired
    private TagDetailService tagDetailService;

    /**
     * @param
     * @return
     * @time 14:45
     * @method
     * @description description
     */
    @RequestMapping(value = "/createESCollection", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON)
    @NoneAuthorization
    public Wrapper createESCollectionParam(@RequestBody CreateESCollectionRequest createESCollectionRequest) {
        boolean flag = esTagService.addTagCollection(createESCollectionRequest.getCollectionName(), createESCollectionRequest.getParam());
        if (!flag) {
            return Wrapper.ERRORBuilder.data("create collection failed!").build();
        }
        return Wrapper.OKBuilder.data("create collection success!").build();
    }

    /**
     * @param
     * @return
     * @time 10:02
     * @method 导入Map格式的数据到ES
     * @description description
     */
    @RequestMapping(value = "/importToEs", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON)
    @NoneAuthorization
    public Wrapper importMapDataToEs(@RequestBody MapDataRequest mapDataRequest) {

        if (mapDataRequest.getDatalist() == null || StringUtils.isEmpty(mapDataRequest.getEsIndexName())) {
            return Wrapper.ERRORBuilder.data("importToEs Failed!").build();
        }

        Integer importCnt = esTagService.bulkImportData(mapDataRequest);
        if (importCnt < 0) {
            return Wrapper.ERRORBuilder.data("import failed").build();
        }
        Map<String, Object> d = Maps.newHashMap();
        d.put("count", importCnt);
        return Wrapper.OKBuilder.data(d).build();
    }

    /**
     * @return
     * @Description: 搜索ES
     * @date 2017年10月25日 下午3:20:03
     */
    @RequestMapping(value = "/searchES", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON)
    @NoneAuthorization
    public Wrapper searchES(@RequestBody TagDetailSearchRequest tagDetailSearchRequest) {
        if (tagDetailSearchRequest.getFrom() == null || tagDetailSearchRequest.getFrom() < 0) {
            tagDetailSearchRequest.setFrom(0);
        }
        if (tagDetailSearchRequest.getSize() == null || tagDetailSearchRequest.getSize() < 0) {
            tagDetailSearchRequest.setSize(10);
        }

        if (!CollectionUtils.isEmpty(tagDetailSearchRequest.getSearchParams())) {

            SearchTagResponse searchResponse = esTagService.searchES(tagDetailSearchRequest);

            return Wrapper.OKBuilder.data(searchResponse).build();
        }
        return Wrapper.ERRORBuilder.data("searchES Fail").build();
    }

    @RequestMapping(value = "/searchWithParent", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON)
    @NoneAuthorization
    public Wrapper searchWithParent(@RequestBody TagDetailSearchRequest tagDetailSearchRequest) {
        if (tagDetailSearchRequest.getFrom() == null || tagDetailSearchRequest.getFrom() < 0) {
            tagDetailSearchRequest.setFrom(0);
        }
        if (tagDetailSearchRequest.getSize() == null || tagDetailSearchRequest.getSize() < 0) {
            tagDetailSearchRequest.setSize(10);
        }

        if (!CollectionUtils.isEmpty(tagDetailSearchRequest.getSearchParams())) {

            SearchTagResponse searchResponse = esTagService.searchWithParent(tagDetailSearchRequest);

            return Wrapper.OKBuilder.data(searchResponse).build();
        }
        return Wrapper.ERRORBuilder.data("searchES Fail").build();
    }

    /**
     * @return
     * @Description: 新增ESTag集合
     * @date 2017年10月25日 下午3:18:17
     */
    @RequestMapping(value = "/addTagCollection", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON)
    @NoneAuthorization
    public Wrapper addTagCollection(@RequestBody TagCollectionRequest tagCollectionRequest) {
        log.info("addTagCollection params : {}", tagCollectionRequest.toString());
        if (StringUtils.isEmpty(tagCollectionRequest.getEsName()) || StringUtils.isEmpty(tagCollectionRequest.getName())
                || tagCollectionRequest.getIsDeleted() == null || StringUtils.isEmpty(tagCollectionRequest.getCreateTime())
                || StringUtils.isEmpty(tagCollectionRequest.getUpdateTime())) {
            return Wrapper.ERRORBuilder.data("paramNull").build();
        }
        TagCollection tagCollection = new TagCollection();
        tagCollection.setName(tagCollectionRequest.getName());
        tagCollection.setEsName(tagCollectionRequest.getEsName());
        tagCollection.setIsDeleted(tagCollectionRequest.getIsDeleted());
        tagCollection.setComment(tagCollectionRequest.getComment());
        tagCollection.setIsDeleted(tagCollectionRequest.getIsDeleted());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            tagCollection.setCreateTime(simpleDateFormat.parse(tagCollectionRequest.getCreateTime()));
            tagCollection.setUpdateTime(simpleDateFormat.parse(tagCollectionRequest.getUpdateTime()));
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
        }
        int result = tagCollectionService.addTagCollection(tagCollection);
        if (result <= 0) {
            return Wrapper.ERRORBuilder.data("add Collection Fail").build();
        }
        return Wrapper.OKBuilder.data("add Collection Sucess").build();
    }

    /**
     * @return
     * @Description: 删除ES Tag集合
     * @date 2017年10月25日 下午3:18:38
     */
    @RequestMapping(value = "/deleteTagCollection/{collectionId}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON)
    @NoneAuthorization
    public Wrapper deleteTagCollection(@PathVariable("collectionId") Integer id) {
        if (id == null) {
            return Wrapper.ERRORBuilder.data("tag Collection ID NULL").build();
        }
        TagCollection tagCollection = new TagCollection();
        tagCollection.setIsDeleted(1);

        TagCollectionExample example = new TagCollectionExample();
        TagCollectionExample.Criteria criteria = example.createCriteria();
        criteria.andIdEqualTo(id);

        int result = tagCollectionService.updateTagCollection(tagCollection, example);

        if (result <= 0) {
            return Wrapper.OKBuilder.data("delet Collection Fail").build();
        }
        return Wrapper.OKBuilder.data("deletSucess").build();
    }

    /**
     * @return
     * @Description: 新增ESTag
     * @date 2017年10月25日 下午3:18:17
     */
    @RequestMapping(value = "/addTag", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON)
    @NoneAuthorization
    public Wrapper addTag(@RequestBody TagDetailAddRequest tagDetailAddRequest) {
        boolean tagDetaiNull = checkObjFieldIsNotNull(tagDetailAddRequest);
        TagDetail tagDetail = new TagDetail();
        tagDetail.setParentId(tagDetailAddRequest.getParentId());
        tagDetail.setCollectionId(tagDetailAddRequest.getCollectionId());
        tagDetail.setFieldName(tagDetailAddRequest.getFieldName());
        tagDetail.setName(tagDetailAddRequest.getName());
        tagDetail.setTagType(tagDetailAddRequest.getTagType());
        tagDetail.setIsDeleted(0);
        tagDetail.setCreateTime(new Date());
        tagDetail.setUpdateTime(new Date());
        tagDetail.setLevel(tagDetailAddRequest.getLevel());
        tagDetail.setComment(tagDetailAddRequest.getComment());

        if (!tagDetaiNull) {

            int result = tagDetailService.addTag(tagDetail);

            if (result <= 0) {
                return Wrapper.ERRORBuilder.data("addTagFail").build();
            }
        }
        return Wrapper.ERRORBuilder.data("addTagFail").build();
    }

    private boolean checkObjFieldIsNotNull(Object obj) {
        boolean flag = false;
        for (Field f : obj.getClass().getDeclaredFields()) {
            f.setAccessible(true);
            try {
                if (f.get(obj) == null) {
                    flag = true;
                    return flag;
                }
            } catch (IllegalAccessException e) {
                log.error(e.getMessage(), e);
            }
        }
        return flag;
    }


    /**
     * @return
     * @Description: 删除ES Tag
     * @date 2017年10月25日 下午3:18:38
     */
    @RequestMapping(value = "/deleteTag/{tagDetailId}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON)
    @NoneAuthorization
    public Wrapper deleteTag(@PathVariable("tagDetailId") Integer tagDetailId) {
        if (tagDetailId == null) {
            return Wrapper.ERRORBuilder.data("tag Detail ID NULL").build();
        }
        TagDetail tagDetail = new TagDetail();
        tagDetail.setIsDeleted(1);

        TagDetailExample example = new TagDetailExample();
        TagDetailExample.Criteria criteria = example.createCriteria();
        criteria.andIdEqualTo(tagDetailId);

        int result = tagDetailService.updateTag(tagDetail, example);
        if (result <= 0) {
            return Wrapper.ERRORBuilder.data("delete TagDetail Error").build();
        }
        return Wrapper.OKBuilder.data("deletSucess").build();
    }

    /**
     * @return
     * @Description: 查询标签集合
     * @date 2017/11/1
     */
    @RequestMapping(value = "/queryTagColleciton/{collectionId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    @NoneAuthorization
    public Wrapper queryTagColleciton(@PathVariable("collectionId") Integer collectionId) {
        log.info("collectionId: {}", collectionId);
        if (collectionId == null) {
            return Wrapper.ERRORBuilder.data("collectionIdNull").build();
        }
        List<TagDetail> tagDetailList = tagDetailService.getTagsByCollectionId(collectionId);
        if (!tagDetailList.isEmpty()) {
            return Wrapper.OKBuilder.data(tagDetailList).build();
        }
        return Wrapper.ERRORBuilder.data("queryTagColleciton Error").build();
    }

}
