package com.haizhi.iap.follow.controller;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Map;

/**
 * @Author dmy
 * @Date 2017/12/21 上午10:47.
 */
@Path("/rest")
public interface NotificationWS {
    @GET
    @Path("/rlationship")
    @Produces({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    Map<String, Object> getRlationship(@QueryParam("company") String company);
}
