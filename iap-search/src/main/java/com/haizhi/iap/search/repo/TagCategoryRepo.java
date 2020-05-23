package com.haizhi.iap.search.repo;

import java.util.Collections;
import java.util.List;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.haizhi.iap.search.model.TagCategory;

/**
 * Created by chenbo on 2017/8/28.
 */
@Slf4j
@Repository
public class TagCategoryRepo {

    private String TABLE_CLUSTER_GROUP = "tag_category";

    @Setter
    @Autowired
    JdbcTemplate template;

    private final RowMapper<TagCategory> TAG_CATEGORY_ROW_MAPPER = new BeanPropertyRowMapper<>(TagCategory.class);
    
    public List<TagCategory> findByCondition(Integer offset, Integer count) {
        StringBuffer buffer = new StringBuffer("select * from " + TABLE_CLUSTER_GROUP + " where 1=1 ");
        List<Object> args = Lists.newArrayList();
       

        if (offset != null && count != null) {
            buffer.append(" limit ?,? ");
            args.add(offset);
            args.add(count);
        }
        try {
            return template.query(buffer.toString(), TAG_CATEGORY_ROW_MAPPER, args.toArray());
        } catch (DataAccessException ex) {
        }
        return Collections.emptyList();
    }


    public List<TagCategory> getAll() {
        return findByCondition(null, null);
    }

}
