package com.haizhi.iap.search.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Created by chenbo on 17/6/22.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DynamicInfo {

    @JsonProperty("doc_id")
    String docId;

    String title;

    String type;

    //fire_add
    @JsonProperty("type_en_name")
    String typeEn;

    @JsonProperty("sub_type_en_name")
    String subTypeEn;

    String date;

    Map<String, Object> detail;

}
