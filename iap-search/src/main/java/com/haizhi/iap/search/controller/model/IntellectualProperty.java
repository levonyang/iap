package com.haizhi.iap.search.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by chenbo on 17/2/15.
 */
@Data
@NoArgsConstructor
public class IntellectualProperty {

    /**
     * 商标
     */
    DataItem trademark;

    /**
     * 专利
     */
    DataItem patent;

    /**
     * 著作权
     */
    DataItem copyright;

    /**
     * 软件著作权
     */
    @JsonProperty("software_copyright")
    DataItem softwareCopyright;
}
