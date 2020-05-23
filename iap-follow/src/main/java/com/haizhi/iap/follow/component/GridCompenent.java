package com.haizhi.iap.follow.component;

import com.haizhi.iap.follow.utils.GridFsOperation;
import com.mongodb.MongoClient;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * @author mtl
 * @Description:
 * @date 2020/3/12 18:09
 */
@Configuration
public class GridCompenent {

    @Setter
    @Value("${gridfs.mongodb.database}")
    String gridfsDBName;

    @Setter
    @Autowired
    @Qualifier(value = "gridfsMongo")
    MongoClient mongo;

    @Lazy
    @Bean
    public GridFsOperation gridFsOperation(){
        return new GridFsOperation(gridfsDBName,mongo);
    }
}
