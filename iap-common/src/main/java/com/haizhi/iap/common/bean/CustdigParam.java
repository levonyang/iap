package com.haizhi.iap.common.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author mtl
 * @Description:
 * @date 2020/3/26 15:57
 */
@Data
@NoArgsConstructor
public class CustdigParam {
    private List<String> companys;
    private String type;
    private int depth;
    private String direct; //in,out,any
}
