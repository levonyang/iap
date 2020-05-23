package com.haizhi.iap.tag.param;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TagDetailSearchRequest {
    List<TagDetailRequest> searchParams;
    Integer from;
    Integer size;
    String esIndexName;
    List<Map<String, Object>> filters;
    String orderBy;
    String order = "DESC";
}
