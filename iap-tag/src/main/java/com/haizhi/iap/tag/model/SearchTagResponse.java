package com.haizhi.iap.tag.model;

import lombok.Data;

import java.util.List;

@Data
public class SearchTagResponse {
    private long totalHit;
    List<TagDoc> docs;
}
