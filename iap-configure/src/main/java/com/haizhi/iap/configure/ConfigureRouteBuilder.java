package com.haizhi.iap.configure;

import org.apache.camel.builder.RouteBuilder;

/**
 * Created by chenbo on 2017/10/10.
 */
public class ConfigureRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        //每五秒轮询被终止的导入任务
        from("direct:stop_import").process("taskAbortedProcessor");

        from("direct:start_import").process("taskStartProcessor");
    }
}
