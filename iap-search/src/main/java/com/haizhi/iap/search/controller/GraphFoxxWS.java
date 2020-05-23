package com.haizhi.iap.search.controller;

import com.haizhi.iap.search.controller.model.GraphReq;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Map;

/**
 * Created by chenbo on 17/2/23.
 */
//@Path("/xyz-test")
@Path("/mytest_new")
public interface GraphFoxxWS {
    @POST
    @Path("/traversal")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Map<String, Object> traversal(@QueryParam("companyId") String companyId,
                                  GraphReq req);

    @POST
    @Path("/shortest-path")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Map<String, Object> shortestPath(@QueryParam("company1") String company1,
                                     @QueryParam("company2") String company2,
                                     GraphReq req);

    @POST
    @Path("/find-path-by-ids")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Map<String, Object> findPathByIds(@QueryParam("bidirectional") boolean bidirectional,
                                      @QueryParam("stop_if_found") boolean stopIfFound,
                                      GraphReq req);

    @GET
    @Path("/check-guarantee-circle")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Map<String, Object> checkGuaranteeCircle(@QueryParam("company1") String company1,
                                             @QueryParam("company2") String company2);

    @GET
    @Path("/find-common-parent")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Map<String, Object> findCommonParent(@QueryParam("company1") String companyA,
                                         @QueryParam("company2") String companyB);

    @GET
    @Path("/explain_actual_control")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Map<String, Object> explainActualControl(@QueryParam("from") String from,
                                             @QueryParam("to") String to);
}
