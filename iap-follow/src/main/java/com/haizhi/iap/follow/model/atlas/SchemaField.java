package com.haizhi.iap.follow.model.atlas;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author mtl
 * @Description:
 * @date 2020/3/14 15:31
 */
@Data
@NoArgsConstructor
public class SchemaField {
    private String field;
    private String fieldNameCn;
    private FieldType type;
}
