package com.haizhi.iap.search.conf;

import com.haizhi.iap.common.utils.ConfUtil;

import java.util.Map;

/**
 * Created by chenbo on 17/3/15.
 */
public class PublicSectorConf {
    private static Map<String, String> sectorConfMap;

    public static Map<String, String> getSectorConfMap() {
        if(sectorConfMap == null){
            sectorConfMap = ConfUtil.getConfMap("sector.conf");
        }
        return sectorConfMap;
    }
}
