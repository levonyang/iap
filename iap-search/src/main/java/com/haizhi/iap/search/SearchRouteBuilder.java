package com.haizhi.iap.search;

import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.ThreadPoolProfileBuilder;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.spi.ThreadPoolProfile;

/**
 * Created by chenbo on 2017/7/22.
 */
public class SearchRouteBuilder extends RouteBuilder {
    //60s 30m/30m20s 1h
    private String interval = "30m";

    @Override
    public void configure() throws Exception {
        ThreadPoolProfile insertThreadPoolProfile = new ThreadPoolProfileBuilder(
                "insertThreadPoolProfile").poolSize(30).maxPoolSize(50).maxQueueSize(100)
                .build();

        ModelCamelContext context = getContext();
        context.getExecutorServiceManager().registerThreadPoolProfile(insertThreadPoolProfile);

        from("timer://timer?period=" + interval + "").process("whiteListProcessor");

        from("direct:cluster_insert").threads()
                .executorServiceRef("insertThreadPoolProfile")
                .setExchangePattern(ExchangePattern.InOnly).process("clusterInsertProcessor");
        from("direct:group_insert").threads()
                .executorServiceRef("insertThreadPoolProfile")
                .setExchangePattern(ExchangePattern.InOnly).process("groupsInsertProcessor");

        from("direct:path_insert").threads()
                .executorServiceRef("insertThreadPoolProfile")
                .setExchangePattern(ExchangePattern.InOnly).process("pathInsertProcessor");
    }

}
