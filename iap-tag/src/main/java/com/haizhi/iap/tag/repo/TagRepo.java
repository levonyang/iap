package com.haizhi.iap.tag.repo;

import com.google.common.collect.Lists;
import com.haizhi.iap.tag.model.TagDetail;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Author dmy
 * @Date 2017/11/2 下午2:32.
 */
@Repository
public class TagRepo {
    @Setter
    @Autowired
    JdbcTemplate template;

    private String TABLE_TAG_DETAIL = "tag_detail";

    private final RowMapper<TagDetail> TAG_DETAIL_ROW_MAPPER = new BeanPropertyRowMapper<>(TagDetail.class);

    public List<TagDetail> findAll() {
        try {
            String sql = "select * from " + TABLE_TAG_DETAIL + " where is_deleted = 0";
            return template.query(sql, TAG_DETAIL_ROW_MAPPER);
        } catch (DataAccessException ex) {
            return Lists.newArrayList();
        }
    }

    public TagDetail findById(Integer id) {
        try {
            String sql = "select * from " + TABLE_TAG_DETAIL + " where id = ? and is_deleted = 0";
            return template.queryForObject(sql, TAG_DETAIL_ROW_MAPPER, id);
        } catch (DataAccessException ex) {
            return null;
        }
    }

    public List<TagDetail> findChildsById(Integer id) {
        try {
            String sql = "select * from " + TABLE_TAG_DETAIL + " where is_deleted = 0 and parent_id = ? ";
            return template.query(sql, TAG_DETAIL_ROW_MAPPER, id);
        } catch (DataAccessException ex) {
            return Lists.newArrayList();
        }
    }
}
