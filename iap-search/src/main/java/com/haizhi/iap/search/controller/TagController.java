package com.haizhi.iap.search.controller;

import java.util.List;

import javax.ws.rs.core.MediaType;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.haizhi.iap.common.Wrapper;
import com.haizhi.iap.common.auth.NoneAuthorization;
import com.haizhi.iap.search.model.TagCategory;
import com.haizhi.iap.search.repo.TagCategoryRepo;

/**
 * Created by chenbo on 16/11/8.
 */
@Api(tags="【搜索-企业标签模块】获取企业标签信息")
@RestController
@RequestMapping(value = "/tag")
public class TagController {
    @Autowired
    TagCategoryRepo tagCategoryRepo;

    @RequestMapping(value = "/", produces = MediaType.APPLICATION_JSON)
    @NoneAuthorization
    public Wrapper ping() {
        return Wrapper.OKBuilder.data("pong").build();
    }

    @RequestMapping(value = "/category", produces = MediaType.APPLICATION_JSON)
    @NoneAuthorization
    public Wrapper category() {
        List<TagCategory> categories = tagCategoryRepo.getAll();
        return Wrapper.OKBuilder.data(categories).build();
    }

}
