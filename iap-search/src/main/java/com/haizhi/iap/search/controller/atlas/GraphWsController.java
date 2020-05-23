package com.haizhi.iap.search.controller.atlas;

import com.haizhi.iap.common.Wrapper;
import com.haizhi.iap.common.auth.NoneAuthorization;
import com.haizhi.iap.search.service.GraphWsService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.core.MediaType;

/**
 * @author mtl
 * @Description:
 * @date 2020/4/2 17:17
 */
@Api(tags="【图平台查询-GDB配置服务】")
@Slf4j
@RestController
@RequestMapping("/atlas/gdb")
public class GraphWsController {

    @Autowired
    private GraphWsService graphWsService;

    @NoneAuthorization
    @RequestMapping(value = "switchGDB",method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON)
    public Wrapper switchGDB(){
        String currentUrl = graphWsService.switchGDB();
        return Wrapper.ok(currentUrl);
    }

//    @NoneAuthorization
//    @RequestMapping(value = "currentGDB",method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON)
//    public Wrapper currentGDB(){
//        Map gdbinfo = graphWsService.currentGDB();
//        return Wrapper.ok(gdbinfo);
//    }
}
