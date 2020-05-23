package com.haizhi.iap.follow.repo;

import com.haizhi.iap.follow.model.ExportImages;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;

/**
 * Created by zhutianpeng on 17/10/14.
 */
@Slf4j
@Repository
public class ExportImagesRepo {
    @Setter
    @Autowired
    @Qualifier(value = "followJdbcTemplate")
    JdbcTemplate template;

    private static final String TABLE_EXPORT_IMAGES = "export_images";

    private RowMapper<ExportImages> EXPORT_IMAGES_ROW_MAPPER = new BeanPropertyRowMapper<>(ExportImages.class);
    public ExportImages create(ExportImages image) {
        try {
            final String sql = "insert into " + TABLE_EXPORT_IMAGES +
                    " (task_id, company, img_path_list, img_intro_list) " +
                    "values (?, ?, ?, ?)";
            GeneratedKeyHolder holder = new GeneratedKeyHolder();
            template.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setLong(1, image.getTaskId());
                ps.setString(2, image.getCompany());
                ps.setString(3, image.getImgPathList());
                ps.setString(4, image.getImgIntroList());
                return ps;
            }, holder);
            if (holder.getKey() != null) {
                image.setId(holder.getKey().longValue());
            }
        } catch (Exception ex) {
            log.error("{}", ex);
        }
        return image;
    }

    public ExportImages findByCompanyNameAndTaskId(String companyName, Long taskId) {
        try {
            String sql = "select * from " + TABLE_EXPORT_IMAGES + " where company = ? and task_id = ?";
            return template.queryForObject(sql, EXPORT_IMAGES_ROW_MAPPER, companyName, taskId);
        } catch (DataAccessException ex) {
        }
        return null;
    }
}
