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
import com.haizhi.iap.search.model.Tag;

/**
 * Created by chenbo on 2017/8/28.
 */
@Slf4j
@Repository
public class TagRepo {

    private String TABLE_TAG = "tag";

    @Setter
    @Autowired
    JdbcTemplate template;

    private final RowMapper<Tag> TAG_ROW_MAPPER = new BeanPropertyRowMapper<>(Tag.class);
    
    public List<Tag> findByCondition(Integer offset, Integer count,Integer categoryId) {
        StringBuffer buffer = new StringBuffer("select * from " + TABLE_TAG + " where category_id = ?");
        List<Object> args = Lists.newArrayList();
        args.add(categoryId);
        if (offset != null && count != null) {	
            buffer.append(" limit ?,? ");
            args.add(offset);
            args.add(count);
        }
        try {
            return template.query(buffer.toString(), TAG_ROW_MAPPER, args.toArray());
        } catch (DataAccessException ex) {
        }
        return Collections.emptyList();
    }
    
    public List<Tag> getAllEnabledTags() {
        StringBuffer buffer = new StringBuffer("select * from tag join tag_category on tag.category_id=tag_category.id where enabled=1 ");
        List<Object> args = Lists.newArrayList();
        try {
            return template.query(buffer.toString(), TAG_ROW_MAPPER, args.toArray());
        } catch (DataAccessException ex) {
        }
        return Collections.emptyList();
    }
    
    public List<Tag> searchTags(Integer categoryId,String keyword) {
        StringBuffer buffer = new StringBuffer("select * from " + TABLE_TAG + " where cn_name like ?");
        List<Object> args = Lists.newArrayList();
        args.add("%"+keyword+"%");
        try {
            return template.query(buffer.toString(), TAG_ROW_MAPPER, args.toArray());
        } catch (DataAccessException ex) {
        }
        return Collections.emptyList();
    }

    public Integer enabledTag(Integer tagID,Integer enabled) {
        StringBuffer buffer = new StringBuffer("update " + TABLE_TAG + " set enabled=? where id=?");
        System.out.println(buffer.toString());
        List<Object> args = Lists.newArrayList();
        args.add(enabled);
        args.add(tagID);
        try {
        	return template.update(buffer.toString(), args.toArray());
        } catch (DataAccessException ex) {
        	System.out.println(ex.toString());
        }
        return 0;
    }
    
    public List<Tag> getEnabledTag(){
        StringBuffer buffer = new StringBuffer("select * from " + TABLE_TAG + " where enabled = 1");
        List<Object> args = Lists.newArrayList();
        try {
            return template.query(buffer.toString(), TAG_ROW_MAPPER, args.toArray());
        } catch (DataAccessException ex) {
        }
        return Collections.emptyList();
    }

}
