package com.haizhi.iap.follow.model.atlas;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @author mtl
 * @Description:
 * @date 2020/3/14 15:26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Edge {
    private String _id;
    private String _from;
    private String _to;
    private String position;
}
