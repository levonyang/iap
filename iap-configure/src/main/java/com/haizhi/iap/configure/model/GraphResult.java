package com.haizhi.iap.configure.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class GraphResult {

    Long count;

    List<Map> list;
}
