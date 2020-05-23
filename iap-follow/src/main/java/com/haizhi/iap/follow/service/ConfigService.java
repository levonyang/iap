package com.haizhi.iap.follow.service;

import com.haizhi.iap.follow.controller.model.InternalWrapper;
import com.haizhi.iap.follow.controller.model.MacroCompanyInfo;
import com.haizhi.iap.follow.model.ChanceSeaInfo;
import com.haizhi.iap.follow.model.MacroNewsInfo;
import com.haizhi.iap.follow.model.PageInfo;
import com.haizhi.iap.follow.model.config.AbstractConfig;
import com.haizhi.iap.follow.model.config.ConfigType;
import com.haizhi.iap.follow.model.config.event.macro.MacroEventConfig;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by chenbo on 2017/12/12.
 */
public interface ConfigService {

    List<AbstractConfig> get(Long userId, ConfigType configType, Integer offset, Integer count);

    List<AbstractConfig> getRisk(Long userId);

    List<AbstractConfig> getMarket(Long userId);

    List<AbstractConfig> getMacro(Long userId);

    MacroEventConfig getMacroByEventType(Long userId, Integer eventType);

    List<AbstractConfig> getConduct(Long userId);

    void saveOrUpdate(Map<String, Object> configMap);

    Map getMatchingMacroInfos(String type, Integer offset, Integer count, boolean isAll);

    Map getMatchingCompanies(String macroId, String type, Integer offset, Integer count);

    boolean storeMacro(Long userId, Integer type, boolean isStore, String macroId);

    Map getMacroStores(Long userId, Integer type, Integer offset, Integer count);

    PageInfo getChanceSeaInfoPage(Integer limit, Integer offset, Boolean collected);

    ChanceSeaInfo getChanceSeaInfoDetail(Long id);

    void changeChanceSeaStatus(Long id, boolean collected);

    AbstractConfig getDefault(Integer type);

}
