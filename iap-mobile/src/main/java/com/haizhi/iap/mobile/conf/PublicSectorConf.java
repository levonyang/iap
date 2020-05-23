package com.haizhi.iap.mobile.conf;

import com.haizhi.iap.common.utils.ConfUtil;

import java.util.Map;

/**
 * Created by chenbo on 17/3/15.
 *
 * 获取股票板块配置信息
 */
public class PublicSectorConf
{
    private static Map<String, String> sectorConfMap;

    public static Map<String, String> getSectorConfMap() {
        if(sectorConfMap == null){
            synchronized (PublicSectorConf.class) {
                if(sectorConfMap == null) sectorConfMap = ConfUtil.getConfMap("sector.conf");
            }
        }
        return sectorConfMap;
    }
}
