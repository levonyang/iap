package com.haizhi.iap.follow.model.atlas;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;

/**
 * @author mtl
 * @Description:
 * @date 2020/3/14 15:24
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Vertex {
    private String _id;
    private String _tag;
    private String name;
    private String _detailUrl;
}
