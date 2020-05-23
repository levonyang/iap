package com.haizhi.iap.search.controller.model;

import com.haizhi.iap.common.utils.BeanUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Created by chenbo on 17/2/23.
 */
@Data
@NoArgsConstructor
public class GraphFilter implements Cloneable {

    Map<String, Object> edge;

    Map<String, Object> vertex;

//    EdgeFilter edge;
//
//    VertexFilter vertex;

    @Override
    protected Object clone() {
        GraphFilter filter = null;
        try {
            filter = (GraphFilter) super.clone();

        } catch (CloneNotSupportedException ex) {
            ex.printStackTrace();
        }
        filter.setEdge((Map<String, Object>) BeanUtil.deepClone(edge));
        filter.setVertex((Map<String, Object>) BeanUtil.deepClone(vertex));
        return filter;
    }
}
