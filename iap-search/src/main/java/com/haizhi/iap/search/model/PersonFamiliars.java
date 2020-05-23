package com.haizhi.iap.search.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Created by chenbo on 17/2/23.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonFamiliars {
    List<Map<String, Object>> relation;

    Map<String, Object> self;
}
