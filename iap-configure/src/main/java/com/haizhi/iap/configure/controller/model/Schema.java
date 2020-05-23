package com.haizhi.iap.configure.controller.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by chenbo on 2017/9/26.
 */
@Data
@NoArgsConstructor
public class Schema {

    List<String> fields;

    List<List<Object>> data;
}
