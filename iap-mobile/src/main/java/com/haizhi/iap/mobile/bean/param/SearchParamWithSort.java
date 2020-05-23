package com.haizhi.iap.mobile.bean.param;

import com.haizhi.iap.mobile.bean.normal.Sort;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Created by thomas on 18/4/11.
 */
@Data
@NoArgsConstructor
public class SearchParamWithSort extends SearchParam
{
    protected List<Sort> sorts;

    @Override
    public Pair<String, String> doValidate()
    {
        Pair<String, String> pair = super.doValidate();
        if(pair != null) return pair;
        if(!CollectionUtils.isEmpty(sorts) && sorts.stream().anyMatch(sort -> StringUtils.isBlank(sort.getField())))
            return Pair.of("sorts", "sort.field不能为空");
        return null;
    }
}
