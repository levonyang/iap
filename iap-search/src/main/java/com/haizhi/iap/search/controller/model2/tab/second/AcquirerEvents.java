package com.haizhi.iap.search.controller.model2.tab.second;

import com.haizhi.iap.search.controller.model.DataItem;
import com.haizhi.iap.search.controller.model2.Counter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by chenbo on 2017/11/9.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcquirerEvents extends Counter {

    DataItem acquirer;

    DataItem acquirered;

}