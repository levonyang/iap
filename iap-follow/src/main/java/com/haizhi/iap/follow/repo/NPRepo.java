package com.haizhi.iap.follow.repo;

import com.google.common.collect.Lists;
import com.haizhi.iap.follow.model.NPScore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @Author dmy
 * @Date 2017/12/18 下午6:33.
 */
@Repository
public class NPRepo {
    @Autowired
    @Qualifier(value = "npJdbcTemplate")
    JdbcTemplate template;

    private static final String TABLE_MACRO_SCORE = "np_macro_score";
    private RowMapper<NPScore> NP_SCORE_ROW_MAPPER = new BeanPropertyRowMapper<>(NPScore.class);

    public List<NPScore> findByType(Integer offset, Integer count, String type, Date start) {
        try {
            StringBuffer buffer = new StringBuffer("select * from " + TABLE_MACRO_SCORE + " where 1 = 1 ");
            List<Object> args = Lists.newArrayList();

            if (type != null) {
                buffer.append(" and type = ? ");
                args.add(type);
            }

            if (start != null) {
                buffer.append(" and utime >= ? ");
                args.add(start);
            }

            buffer.append(" and score > 1 order by utime desc, score desc ");

            if (count != null) {
                buffer.append(" limit ?, ? ");
                args.add(offset);
                args.add(count);
            }
            return template.query(buffer.toString(), NP_SCORE_ROW_MAPPER, args.toArray());
        } catch (DataAccessException ex) {

        }
        return Collections.EMPTY_LIST;
    }

    public Integer getCountByType(String type, Date start) {
        try {
            StringBuffer buffer = new StringBuffer("select count(*) from " + TABLE_MACRO_SCORE + " where 1 = 1 ");
            List<Object> args = Lists.newArrayList();

            if (type != null) {
                buffer.append(" and type = ? ");
                args.add(type);
            }

            if (start != null) {
                buffer.append(" and utime >= ? ");
                args.add(start);
            }

            buffer.append(" and score > 1 ");

            return template.queryForObject(buffer.toString(), Integer.class, args.toArray());
        } catch (DataAccessException ex) {
            return -1;
        }
    }
}
