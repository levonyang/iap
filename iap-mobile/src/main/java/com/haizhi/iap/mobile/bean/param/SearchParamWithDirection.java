package com.haizhi.iap.mobile.bean.param;

import com.haizhi.iap.mobile.bean.normal.GraphEdge;
import lombok.Data;

/**
 * Created by thomas on 18/4/18.
 *
 * 带图谱方向的查找参数
 */
@Data
public class SearchParamWithDirection extends SearchParam
{
    private GraphEdge.Direction direction = GraphEdge.Direction.ALL;
}
