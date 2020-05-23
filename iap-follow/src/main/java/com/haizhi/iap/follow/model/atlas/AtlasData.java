package com.haizhi.iap.follow.model.atlas;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @author mtl
 * @Description:
 * @date 2020/3/14 18:36
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AtlasData {
    private List<Map> vertices;
    private List<Map> edges;
    private List<Schema> schemas;
    private List<UiConfig> uiConfigs;
}
