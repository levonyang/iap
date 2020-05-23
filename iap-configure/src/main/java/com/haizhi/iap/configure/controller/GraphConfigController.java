package com.haizhi.iap.configure.controller;

import com.google.common.base.Strings;
import com.haizhi.iap.common.Wrapper;
import com.haizhi.iap.common.auth.NoneAuthorization;
import com.haizhi.iap.configure.exception.ConfigException;
import com.haizhi.iap.configure.model.GraphRelation;
import com.haizhi.iap.configure.model.SearchGraphParam;
import com.haizhi.iap.configure.service.DataSourceService;
import com.haizhi.iap.configure.service.GraphRelationService;
import io.swagger.annotations.Api;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.core.MediaType;


@Api(tags="【数据配置-行内关系模块】对行内关系进行管理")
@Slf4j
@RestController
@RequestMapping(value = "/config/graph")
public class GraphConfigController {

    @Setter
    @Autowired
    GraphRelationService graphRelationService;

    @RequestMapping(value = "/save", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper saveGraph(@RequestBody GraphRelation graphRelation) {
        if (Strings.isNullOrEmpty(graphRelation.getName())) {
            return ConfigException.MISS_NAME.get();
        }
        if (graphRelation.getSourceConfigId() == null) {
            return ConfigException.MISS_SOURCE_CONFIG_ID.get();
        }

        try {
            GraphRelation ret = graphRelationService.saveGraphRelation(graphRelation);
            return Wrapper.OKBuilder.data(ret).build();
        } catch (Exception e) {
            return Wrapper.ERRORBuilder.msg(e.getMessage()).build();
        }
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper listRelations(@RequestParam(value = "graphId", required = false) Integer graphId) {
        return Wrapper.OKBuilder.data(graphRelationService.getGraphRelation(graphId)).build();
    }

    @RequestMapping(value = "/delete/{graphId}", method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper deleteRelation(@PathVariable("graphId") Integer graphId) {
        if (graphId == null) {
            return ConfigException.MISS_GRAPH_ID.get();
        }
        boolean ret = graphRelationService.deleteGraphRelation(graphId);
        return ret == true ? Wrapper.OK : Wrapper.ERROR;
    }

    @RequestMapping(value = "/graph_config/{graphName}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper getGraphConfig(@PathVariable("graphName") String graphName) {
        if (Strings.isNullOrEmpty(graphName)) {
            return ConfigException.MISS_GRAPH_NAME.get();
        }
        return Wrapper.OKBuilder.data(graphRelationService.getGraphConfig(graphName)).build();
    }

    @RequestMapping(value = "/demo/{graphName}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper getDemo(@PathVariable("graphName") String graphName) {
        if (Strings.isNullOrEmpty(graphName)) {
            return ConfigException.MISS_GRAPH_NAME.get();
        }
        return Wrapper.OKBuilder.data(graphRelationService.getDemo(graphName)).build();
    }

    @RequestMapping(value = "/query", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper queryGraphDetail(@RequestBody SearchGraphParam searchGraphParam) {
        if (Strings.isNullOrEmpty(searchGraphParam.getTable())) {
            return ConfigException.MISS_GRAPH_NAME.get();
        }
        if (Strings.isNullOrEmpty(searchGraphParam.getFrom()) || Strings.isNullOrEmpty(searchGraphParam.getTo())) {
            return ConfigException.MISS_FROM_TO_MAP.get();
        }
        if (searchGraphParam.getLimit() <= 0 || searchGraphParam.getLimit() > 100) {
            searchGraphParam.setLimit(10);
        }
        return Wrapper.OKBuilder.data(graphRelationService.queryGraphDetail(searchGraphParam)).build();
    }

    @RequestMapping(value = "/demo_grelation", method = RequestMethod.GET)
    public Wrapper getDemoGraphRelation(@RequestParam(value = "grelationID") Integer grelationID) {
        return Wrapper.OKBuilder.data(graphRelationService.getDemoGRelation(grelationID)).build();
    }
}
