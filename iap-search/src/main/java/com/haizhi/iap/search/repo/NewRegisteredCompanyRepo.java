package com.haizhi.iap.search.repo;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.haizhi.iap.search.model.NewRegisteredCompany;
import com.haizhi.iap.search.utils.DateUtil;
import com.haizhi.iap.search.utils.DateUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Repository
public class NewRegisteredCompanyRepo {

    private String TABLE_NEW_COMPANY = "new_registered_company";

    @Setter
    @Autowired
    JdbcTemplate template;

    private final RowMapper<NewRegisteredCompany> TAG_ROW_MAPPER = new BeanPropertyRowMapper<>(NewRegisteredCompany.class);

    public List<NewRegisteredCompany> findByCondition(Integer offset, Integer count, Integer type) {
        StringBuffer buffer = new StringBuffer("select * from " + TABLE_NEW_COMPANY + " where type = ?");
        List<Object> args = Lists.newArrayList();
        args.add(type);
        if (type == 0) {
            String date = DateUtil.getFormat1().format(DateUtil.addMonth(new Date(), -1));
            buffer.append(" and created_date >= ? ");
            args.add(date);
        }
        if (offset != null && count != null) {
            buffer.append(" limit ?,? ");
            args.add(offset);
            args.add(count);
        }
        try {
            return template.query(buffer.toString(), TAG_ROW_MAPPER, args.toArray());
        } catch (DataAccessException ex) {
            ex.printStackTrace();
        }
        return Collections.emptyList();
    }

    public Map<String, Object> count(Integer type) {
        StringBuffer buffer = new StringBuffer("select count(*) as num from " + TABLE_NEW_COMPANY + " where `type` = ? ");
        if (type == 0) {
            String date = DateUtil.getFormat1().format(DateUtil.beforeXDay(new Date(), 30));
            buffer.append(" and created_date >= ?");
            return template.queryForMap(buffer.toString(), type, date);
        }
        try {
            return template.queryForMap(buffer.toString(), type);
        } catch (DataAccessException ex) {
            //System.out.println(ex.toString());
            return new HashMap<String, Object>() {
                {
                    put("num", 0);
                }
            };
        }
    }

    public List<NewRegisteredCompany> findByCompanyInGroupEnterprise(Set<String> groupEnterpriseSet, Integer type) {
        if (CollectionUtils.isEmpty(groupEnterpriseSet)) {
            return Collections.emptyList();
        }
        String sqlInParam = CharMatcher.is(',').trimFrom(Strings.repeat("?,", groupEnterpriseSet.size()));
        String sql = "select * from " + TABLE_NEW_COMPANY + " where type = ? and company in (" + sqlInParam + ")";
        List<Object> params = Lists.newArrayList();
        params.add(type);
        params.addAll(groupEnterpriseSet);
        if (type == 0) {
            sql = sql + " and created_date >= ?";
            // 1个月以内的新注册企业
            String date = LocalDate.now().minusMonths(1L).format(DateUtils.YYYY_MM_DD);
            params.add(date);
        }
        try {
            return template.query(sql, TAG_ROW_MAPPER, params.toArray());
        } catch (DataAccessException ex) {
            log.error("{} : {}", ex.getClass().getName(), ex.getMessage());
            ex.printStackTrace();
        }
        return Collections.emptyList();
    }
}
