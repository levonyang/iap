package com.haizhi.iap.mobile.bean.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * Created by thomas on 18/3/21.
 */
@Data
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class Graph2
{
    List<Map<String, Object>> vertexes;
    List<Map<String, Object>> edges;
}
