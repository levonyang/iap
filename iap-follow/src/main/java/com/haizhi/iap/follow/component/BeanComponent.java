package com.haizhi.iap.follow.component;

import com.haizhi.iap.follow.repo.RedisRepo;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by chenbo on 2017/10/24.
 */
@Component
public class BeanComponent {

    @Setter
    @Autowired
    RedisRepo redisRepo;

    @PostConstruct
    public void init(){
        redisRepo.savePermanentToken();
    }
}