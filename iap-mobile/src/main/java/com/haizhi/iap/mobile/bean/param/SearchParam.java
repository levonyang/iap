package com.haizhi.iap.mobile.bean.param;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Created by thomas on 18/4/11.
 */
@Data
@NoArgsConstructor
public class SearchParam extends BasicParam implements Cloneable
{
    protected String keyword;
    protected Integer offset = 0;
    protected Integer size = 3;
    //protected String sort = null;

    public SearchParam(String username, String keyword, Integer offset, Integer size)
    {
        this.username = username;
        this.keyword = keyword;
        this.offset = offset;
        this.size = size;
    }

    @Override
    public Pair<String, String> doValidate()
    {
        if(offset != null && offset < 0) return Pair.of("offset", "offset不能为负数");
        if(size != null && size < 1) return Pair.of("size", "size不能小于1");
        if(StringUtils.isBlank(keyword)) return Pair.of("keyword", "keyword不能为空");
        return null;
    }
}
