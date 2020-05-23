package com.haizhi.iap.tag.controller;

import com.haizhi.iap.common.Wrapper;
import com.haizhi.iap.common.auth.NoneAuthorization;
import com.haizhi.iap.tag.controller.param.TagParticipleParam;
import com.haizhi.iap.tag.model.TagInfo;
import com.haizhi.iap.tag.service.TagInfoService;
import io.swagger.annotations.Api;
import lombok.Setter;
import org.elasticsearch.common.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * @Author dmy
 * @Date 2017/11/2 下午8:39.
 */
@Api(tags="【标签-标签模块】标签操作")
@RestController
@RequestMapping(value = "/tag")
public class TagController {

    @Setter
    @Autowired
    TagInfoService tagInfoService;

    @RequestMapping(value = "/dictionary", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON)
    @NoneAuthorization
    public Wrapper updateTagDictionary() {
        tagInfoService.buildTagDictionary();
        return Wrapper.OK;
    }

    @RequestMapping(value = "/in_content", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON)
    @NoneAuthorization
    public Wrapper findTagFromContent(@RequestBody TagParticipleParam tagParticipleParam) {
        List<TagInfo> tagInfos = null;
        if (!Strings.isNullOrEmpty(tagParticipleParam.getContent())) {
            tagInfos = tagInfoService.findTagFromContent(tagParticipleParam);
        }
        return Wrapper.OKBuilder.data(tagInfos).build();
    }
}
