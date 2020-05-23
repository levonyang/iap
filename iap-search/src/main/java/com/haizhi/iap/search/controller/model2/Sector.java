package com.haizhi.iap.search.controller.model2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by chenbo on 2017/11/7.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sector {
    //上市板块
    String sector;

    //股票代码
    @JsonProperty("stock_code")
    String stockCode;
}
