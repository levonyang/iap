package com.haizhi.iap.mobile.component;

import com.haizhi.iap.mobile.bean.CustomerBelong;
import com.haizhi.iap.mobile.bean.FollowItem;
import com.haizhi.iap.mobile.bean.MarketEventSetting;
import com.haizhi.iap.mobile.bean.Notification;
import com.haizhi.iap.mobile.repo.BasicSqlRepo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by thomas on 18/4/13.
 */
@Configuration
public class SqlRepoConfiguration
{
    @Bean
    public BasicSqlRepo<FollowItem> followItemRepo()
    {
        return new BasicSqlRepo<>(FollowItem.class);
    }

    @Bean
    public BasicSqlRepo<Notification> notificationRepo()
    {
        return new BasicSqlRepo<>(Notification.class);
    }

    @Bean
    public BasicSqlRepo<MarketEventSetting> marketEventSettingRepo()
    {
        return new BasicSqlRepo<>(MarketEventSetting.class);
    }

    @Bean
    public BasicSqlRepo<CustomerBelong> customerBelongRepo()
    {
        return new BasicSqlRepo<>((resultSet, rowNum) -> new CustomerBelong(
                resultSet.getString("CUST_ID"),
                resultSet.getString("CUST_NAME"),
                resultSet.getString("MGMT_CUSTM_ID"),
                resultSet.getString("RELA_TYPE"),
                resultSet.getString("LOAD_DT")
        ));
    }
}
