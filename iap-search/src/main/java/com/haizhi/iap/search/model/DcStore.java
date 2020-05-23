package com.haizhi.iap.search.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author mtl
 * @Description:
 * @date 2020/4/2 18:01
 */
@Data
@NoArgsConstructor
@ToString
public class DcStore {

    private String name;
    private String url;
    private String user_name;
    private String password;
    private String type;
}
