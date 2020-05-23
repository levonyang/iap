package com.haizhi.iap.search.controller;

import com.haizhi.iap.search.controller.model.GraphQuery;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Map;

/**
 * Created by chenbo on 17/2/24.
 */
@Path("/_api")
public interface GraphWS {
    @POST
    @Path("/cursor")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Map<String, Object> query(GraphQuery query);
}
