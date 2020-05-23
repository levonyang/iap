package com.haizhi.iap.tag.dao;

import com.haizhi.iap.tag.model.TagDetail;
import com.haizhi.iap.tag.model.TagDetailExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TagDetailMapper {
    long countByExample(TagDetailExample example);

    int deleteByExample(TagDetailExample example);

    int insert(TagDetail record);

    int insertSelective(TagDetail record);

    List<TagDetail> selectByExampleWithBLOBs(TagDetailExample example);

    List<TagDetail> selectByExample(TagDetailExample example);

    int updateByExampleSelective(@Param("record") TagDetail record, @Param("example") TagDetailExample example);

    int updateByExampleWithBLOBs(@Param("record") TagDetail record, @Param("example") TagDetailExample example);

    int updateByExample(@Param("record") TagDetail record, @Param("example") TagDetailExample example);
}