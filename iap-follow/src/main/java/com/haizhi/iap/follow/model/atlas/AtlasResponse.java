package com.haizhi.iap.follow.model.atlas;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author mtl
 * @Description:
 * @date 2020/3/14 18:16
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AtlasResponse {
    private String success;
    private String message;
    private Object payload;
    private String version;
}
