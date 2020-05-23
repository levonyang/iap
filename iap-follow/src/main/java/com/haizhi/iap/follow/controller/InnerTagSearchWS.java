package com.haizhi.iap.follow.controller;

import com.haizhi.iap.follow.controller.model.InternalWrapper;
import com.haizhi.iap.follow.controller.model.TagDetailSearchRequest;
import com.haizhi.iap.follow.controller.model.TagParticipleParam;
import org.springframework.web.bind.annotation.RequestBody;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @Author dmy
 * @Date 2017/12/19 上午12:41.
 */
@Path("/")
public interface InnerTagSearchWS {

    @POST
    @Path("/engine/searchWithParent")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    InternalWrapper searchES(@RequestBody TagDetailSearchRequest tagDetailSearchRequest);

    @POST
    @Path("/tag/in_content")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    InternalWrapper findTagFromContent(@RequestBody TagParticipleParam tagParticipleParam);
}
