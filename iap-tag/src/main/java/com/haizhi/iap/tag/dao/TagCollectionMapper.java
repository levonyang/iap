package com.haizhi.iap.tag.dao;

import com.haizhi.iap.tag.model.TagCollection;
import com.haizhi.iap.tag.model.TagCollectionExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TagCollectionMapper {
    long countByExample(TagCollectionExample example);

    int deleteByExample(TagCollectionExample example);

    int insert(TagCollection record);

    int insertSelective(TagCollection record);

    List<TagCollection> selectByExampleWithBLOBs(TagCollectionExample example);

    List<TagCollection> selectByExample(TagCollectionExample example);

    int updateByExampleSelective(@Param("record") TagCollection record, @Param("example") TagCollectionExample example);

    int updateByExampleWithBLOBs(@Param("record") TagCollection record, @Param("example") TagCollectionExample example);

    int updateByExample(@Param("record") TagCollection record, @Param("example") TagCollectionExample example);
}