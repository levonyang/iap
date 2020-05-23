package com.haizhi.iap.follow.repo;

import com.google.common.collect.Lists;
import com.haizhi.iap.follow.model.Tag;
import lombok.Setter;
import org.elasticsearch.common.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

/**
 * Created by chenbo on 2017/12/14.
 */
@Repository
public class TagRepo {
    @Setter
    @Autowired
    @Qualifier("tagJdbcTemplate")
    JdbcTemplate template;

    private static String CATEGORY_AREA = "tag_district";
    private static String CATEGORY_INDUSTRY = "tag_economic";
    private static String CATEGORY_PRODUCT = "tag_product";
    private static String CATEGORY_STOCK = "tag_stock";

    private static String TABLE_TAG_DETAIL = "tag_detail";

    private RowMapper<Tag> TAG_DETAIL_ROW_MAPPER = new BeanPropertyRowMapper<>(Tag.class);

    public List<Tag> getArea(String keyword, Boolean isHot, Integer offset, Integer count) {
        return findByCondition(CATEGORY_AREA, keyword, isHot, offset, count);
    }

    public List<Tag> getIndustry(String keyword, Boolean isHot, Integer offset, Integer count) {
        return findByCondition(CATEGORY_INDUSTRY, keyword, isHot, offset, count);
    }

    public List<Tag> getProduct(String keyword, Boolean isHot, Integer offset, Integer count) {
        return findByCondition(CATEGORY_PRODUCT, keyword, isHot, offset, count);
    }

    public List<Tag> getStock(String keyword, Boolean isHot, Integer offset, Integer count) {
        return findByCondition(CATEGORY_STOCK, keyword, isHot, offset, count);
    }

    public List<Tag> findByCondition(String category, String keyword, Boolean isHot, Integer offset, Integer count) {
        try {
            StringBuilder buffer = new StringBuilder("select * from " + TABLE_TAG_DETAIL + " where 1=1 ");
            List<Object> args = Lists.newArrayList();

            if (category != null && category.equals(CATEGORY_PRODUCT)) {
                buffer.append(" and `level` <= 3 ");
            }else {
                buffer.append(" and `level` <= 2 ");
            }

            if (category != null) {
                buffer.append(" and tag_class = ? ");
                args.add(category);
            }
            if (keyword != null) {
                buffer.append(" and name like ? ");
                args.add("%" + keyword + "%");
            }
            if (isHot != null) {
                buffer.append(" and is_hot = ? ");
                args.add(isHot ? 1 : 0);
            }
            if (offset != null && count != null) {
                buffer.append(" limit ?,? ");
                args.add(offset);
                args.add(count);
            }
            return template.query(buffer.toString(), TAG_DETAIL_ROW_MAPPER, args.toArray());
        } catch (DataAccessException ex) {
            return Collections.emptyList();
        }
    }

    public Tag getById(Long id) {
        try {
            String sql = "select * from " + TABLE_TAG_DETAIL + " where id = ? ";
            return template.queryForObject(sql, TAG_DETAIL_ROW_MAPPER, id);
        } catch (DataAccessException ex) {
            return null;
        }
    }
}
