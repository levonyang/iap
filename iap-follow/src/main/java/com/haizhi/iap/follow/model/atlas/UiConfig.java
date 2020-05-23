package com.haizhi.iap.follow.model.atlas;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author mtl
 * @Description:
 * @date 2020/3/14 15:34
 */
@Data
@NoArgsConstructor
public class UiConfig {
    private String schema;
    private String style;
    private String color;
    private SizeType size;
}
