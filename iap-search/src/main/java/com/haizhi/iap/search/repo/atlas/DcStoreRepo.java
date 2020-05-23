package com.haizhi.iap.search.repo.atlas;

import com.haizhi.iap.search.model.DcStore;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author mtl
 * @Description:
 * @date 2020/4/2 17:24
 */
@Repository
public class DcStoreRepo {

    @Setter
    @Autowired
    @Qualifier("dcStoreJdbcTemplate")
    private JdbcTemplate template;

    private RowMapper<DcStore> DCSTORE_ROWMAPPER = new BeanPropertyRowMapper<>(DcStore.class);

    /**
     * 查询数据库列表
     * @param dbType
     * @return
     */
    public DcStore findDcStore(String dbType){
        String sql = "select * from dc_store where type = ? ";
        List<DcStore> list = this.template.query(sql, new Object[]{dbType}, DCSTORE_ROWMAPPER);
        if(list.size() > 0){
            return list.get(0);
        }
        return null;
    }
}
