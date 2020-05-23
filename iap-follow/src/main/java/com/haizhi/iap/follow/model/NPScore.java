package com.haizhi.iap.follow.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

/**
 * @Author dmy
 * @Date 2017/12/19 下午3:51.
 */
@Data
public class NPScore {
    Long id;

    @JsonProperty("macro_id")
    String macroId;

    Double score;

    Date utime;

    String type;
}
