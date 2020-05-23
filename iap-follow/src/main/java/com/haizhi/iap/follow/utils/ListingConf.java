package com.haizhi.iap.follow.utils;

import com.haizhi.iap.common.utils.ConfUtil;
import com.haizhi.iap.follow.repo.AppDataCollections;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class ListingConf {
    private static Map<String, String> ssgsBaseInfoMap;
    private static Map<String, String> caiBaoMap;

    public static Map<String, String> getSsgsBaseInfoMap() {
        if (ssgsBaseInfoMap == null) {
            ssgsBaseInfoMap = ConfUtil.getConfMap("ssgs_baseinfo.conf");
        }
        return ssgsBaseInfoMap;
    }

    public static Map<String, String> getCaiBaoMap(){
        if(caiBaoMap == null){
            caiBaoMap = ConfUtil.getConfMap("ssgs_caibao.conf");
        }
        return caiBaoMap;
    }
}