package com.haizhi.iap.follow.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @Author dmy
 * @Date 2017/12/20 下午8:59.
 */
@Data
public class MacroCompanyInfo {
    String id;

    String name;

    @JsonProperty("key_words")
    String keyWords;

    String relation;

    String company;
}
