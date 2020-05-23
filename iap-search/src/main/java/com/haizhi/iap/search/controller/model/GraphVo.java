package com.haizhi.iap.search.controller.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by thomas on 18/3/21.
 */
@Data
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value="图查询结果", description="用于表示图查询结果")
public class GraphVo {
    @ApiModelProperty(value = "实体列表")
    List<Map<String, Object>> vertexes = new ArrayList<>();

    @ApiModelProperty(value = "关系列表")
    List<Map<String, Object>> edges = new ArrayList<>();
}
