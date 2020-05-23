package com.haizhi.iap.follow;

import lombok.Setter;
import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.ThreadPoolProfileBuilder;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.spi.ThreadPoolProfile;
import org.springframework.beans.factory.annotation.Value;

public class FollowRouteBuilder extends RouteBuilder {
    @Setter
//    @PropertyInject("quartz.interval")
    @Value("${quartz.interval}")
    String interval;

    @Override
        public void configure() throws Exception {
        ThreadPoolProfile insertThreadPoolProfile = new ThreadPoolProfileBuilder(
                "insertThreadPoolProfile").poolSize(30).maxPoolSize(50).maxQueueSize(100)
                .build();

        ThreadPoolProfile crawThreadPoolProfile = new ThreadPoolProfileBuilder(
                "crawThreadPoolProfile").poolSize(30).maxPoolSize(50).maxQueueSize(100)
                .build();

        ModelCamelContext context = getContext();
        context.getExecutorServiceManager().registerThreadPoolProfile(insertThreadPoolProfile);
        context.getExecutorServiceManager().registerThreadPoolProfile(crawThreadPoolProfile);

        //轮询task,处理未启动的task
        from("timer://timer?period=" + interval + "").process("taskProcessor");

        from("direct:batch_insert").threads()
                .executorServiceRef("insertThreadPoolProfile")
                .setExchangePattern(ExchangePattern.InOnly).process("batchInsertProcessor");

        from("direct:batch_craw").threads()
                .executorServiceRef("crawThreadPoolProfile")
                .setExchangePattern(ExchangePattern.InOnly).process("batchCrawProcessor");
        //TODO 删除关注项 删除消息
        //from("direct:task_add").process("TaskProcessor");
    }

    @Override
    public ModelCamelContext getContext() {
        return super.getContext();
    }
}