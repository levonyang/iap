package com.haizhi.iap.search.controller.model2.tab.second;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.haizhi.iap.search.controller.model.DataItem;
import com.haizhi.iap.search.controller.model2.Counter;

/**
 * Created by chenbo on 2017/11/8.
 */
public class IntellectualProperty extends Counter {
    //商标
    DataItem trademark;

    //专利
    DataItem patent;

    //著作权
    DataItem copyright;

    //软件著作权
    @JsonProperty("software_copyright")
    DataItem softwareCopyright;

}
