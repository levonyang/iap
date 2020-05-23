package com.haizhi.iap.follow.model.atlas;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author mtl
 * @Description:
 * @date 2020/3/14 15:28
 */
@Data
@NoArgsConstructor
public class Schema {
    private String schema;
    private String schemaNameCn;
    private SchemaType type;
    private String displayField;
    private List<SchemaField> fields;
}
