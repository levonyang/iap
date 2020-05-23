package com.haizhi.iap.mobile.bean.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by thomas on 18/4/11.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HasMoreResult<T>
{
    private long total;
    private boolean hasMore;
    private T results;
}
