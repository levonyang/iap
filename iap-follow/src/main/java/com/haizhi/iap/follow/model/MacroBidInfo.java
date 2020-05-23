package com.haizhi.iap.follow.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author dmy
 * @Date 2017/12/26 下午9:04.
 */
@Data
public class MacroBidInfo {
    @JsonProperty("macro_id")
    String macroId;

    String title;

    String district;

    @JsonProperty("bid_companys")
    List bidCompanys;

    @JsonProperty("key_words")
    String keyWords;

    String date;

    @JsonProperty("is_store")
    boolean store;
}
