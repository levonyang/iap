package com.haizhi.iap.common.config;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.ContextLoaderListener;

import javax.servlet.ServletContextEvent;

@Slf4j
public class SystemProfileListener extends ContextLoaderListener {

    private String DEFAULT_PROFILE_ACTIVE = "dev";
    private String PROFILE_KEY = "profiles.active";

    @Override
    public void contextInitialized(ServletContextEvent event) {
        loadEnvironment(event);
        super.contextInitialized(event);
    }

    /**
     * 加载启动环境
     *
     * @param event
     */
    private void loadEnvironment(ServletContextEvent event) {
        String active;
        //以系统参数为主
        if (!Strings.isNullOrEmpty(System.getProperty(PROFILE_KEY))) {
            active = System.getProperty(PROFILE_KEY);
        } else {
            String springProfileActive = event.getServletContext().getInitParameter("spring.profiles.active");
            if (Strings.isNullOrEmpty(springProfileActive) || "${profiles.active}".equals(springProfileActive)) {
                System.getProperties().setProperty(PROFILE_KEY, DEFAULT_PROFILE_ACTIVE);
                active = DEFAULT_PROFILE_ACTIVE;
            } else {
                System.getProperties().setProperty(PROFILE_KEY, springProfileActive);
                active = springProfileActive;
            }
        }
        log.info("==== system property of profiles active: {} ====", active);
    }
}