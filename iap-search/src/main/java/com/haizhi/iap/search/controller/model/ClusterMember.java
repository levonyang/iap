package com.haizhi.iap.search.controller.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Data
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class ClusterMember {
    List<Map<String, Object>> vertexes;
}
