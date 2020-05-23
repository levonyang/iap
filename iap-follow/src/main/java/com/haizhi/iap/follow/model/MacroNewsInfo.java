package com.haizhi.iap.follow.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @Author dmy
 * @Date 2017/12/19 上午11:43.
 */
@Data
public class MacroNewsInfo {

    @JsonProperty("macro_id")
    String macroId;

    String title;

    String sentiment;

    @JsonProperty("key_words")
    String keyWords;

    String date;

    @JsonProperty("is_store")
    boolean store;
}
