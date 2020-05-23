package com.haizhi.iap.follow.controller;

import com.haizhi.iap.common.bean.CustdigParam;
import com.haizhi.iap.follow.controller.model.InternalWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PathVariable;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by chenbo on 2017/9/28.
 */
@Path("/internal")
public interface InternalSearchWS {
    @GET
    @Path("/search")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    InternalWrapper search(@QueryParam("name") String name,
                           @QueryParam(value = "stock_code") String stockCode,
                           @QueryParam("type") String type,
                           @QueryParam(value = "sub_type") String subType,
                           @QueryParam(value = "third_type") String thirdType,
                           @QueryParam(value = "year_quarter") String yearQuarter,
                           @QueryParam(value = "only_count") Integer onlyCount,
                           @QueryParam(value = "offset") Integer offset,
                           @QueryParam(value = "count") Integer count);
    @POST
    @Path("/custdig")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    InternalWrapper custdig(CustdigParam custdigParam);

    @POST
    @Path("/findCustByname")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    InternalWrapper findCustByname(List<String> names);
}
