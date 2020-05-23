package com.haizhi.iap.proxy.controller;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Map;

/**
 * Created by chenbo on 17/2/17.
 */
@Path("/api")
public interface DeepSearchWS {
    /**
     * 查询抓取状态
     *
     * @return
     */
    @GET
    @Path("/get_schedule_list")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Map listSchedule(@QueryParam("user") String user,
                     @QueryParam("offset") String offset,
                     @QueryParam("count") String count,
                     @QueryParam("schedule_type") String scheduleType);

    /**
     * 查询抓取状态
     *
     * @return
     */
    @GET
    @Path("/queryupdatestatus")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Map queryStatus(@QueryParam("company") String company);

    /**
     * 上报更新
     *
     * @return
     */
    @POST
    @Path("/updatecompanydata")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Map getNewDetail(Map<String, Object> param);

    /**
     * 获取优先级对应的count信息
     */
    @GET
    @Path("/query_schedule_level_count")
    Map<String, Object> levelAndCount();

    @GET
    @Path("/del/single_company")
    Map cancelCrawl(@QueryParam("company") String company,
                    @QueryParam("user") String user);

    @GET
    @Path("/is_posted")
    Map isPosted(@QueryParam("company") String company,
                 @QueryParam("user") String user);
}
