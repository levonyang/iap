package com.haizhi.iap.follow.controller.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @Author dmy
 * @Date 2017/12/19 上午12:46.
 */
@Data
public class TagDetailSearchRequest {
    List<TagDetailRequest> searchParams;
    Integer from = 0;
    Integer size;
    String esIndexName;
    List<Map<String, Object>> filters;
    String orderBy;
    String order = "DESC";

}
