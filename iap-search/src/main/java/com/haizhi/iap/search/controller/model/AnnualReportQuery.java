package com.haizhi.iap.search.controller.model;

import lombok.Data;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Set;

/**
 * Created by thomas on 18/3/22.
 */
@Data
public class AnnualReportQuery
{
    @NotNull
    @Size(min = 1)
    private Set<String> names;
    private Integer offset;
    private Integer size;
    private List<String> fields;
    private Sort sort;

    @Data
    public static class Sort
    {
        private String field;
        private Order order;
    }

    public enum Order
    {
        ASC, DESC
    }

    public String validate()
    {
        if(offset != null && offset < 0) return "offset不能小于0";
        if(size != null && size < 0) return "size不能小于0";
        if(!CollectionUtils.isEmpty(fields))
        {
            for (String field : fields)
                if(StringUtils.isEmpty(field)) return "fields中的元素不能为空";
        }
        if(sort != null)
        {
            if(StringUtils.isEmpty(sort.field)) return "sort.field不能为空";
        }
        return "";
    }
}
