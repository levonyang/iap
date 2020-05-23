package com.haizhi.iap.follow.controller;

import com.google.common.collect.Maps;
import com.haizhi.iap.common.Wrapper;
import com.haizhi.iap.common.auth.DefaultSecurityContext;
import com.haizhi.iap.follow.controller.model.InternalWrapper;
import com.haizhi.iap.follow.controller.model.MacroCompanyInfo;
import com.haizhi.iap.follow.controller.model.TagFamily;
import com.haizhi.iap.follow.exception.FollowException;
import com.haizhi.iap.follow.model.*;
import com.haizhi.iap.follow.model.config.AbstractConfig;
import com.haizhi.iap.follow.model.config.ConfigType;
import com.haizhi.iap.follow.model.config.event.macro.MacroEventConfig;
import com.haizhi.iap.follow.model.config.event.market.MarketEventConfig;
import com.haizhi.iap.follow.model.config.event.risk.RiskEventConfig;
import com.haizhi.iap.follow.model.config.rule.conduct.ConductConfig;
import com.haizhi.iap.follow.service.ConfigService;
import com.haizhi.iap.follow.service.TagService;
import io.swagger.annotations.Api;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

/**
 * Created by chenbo on 2017/12/12.
 */
@Api(tags="【关注-配置模块】关注列表管理")
@RestController
@RequestMapping("/follow/config")
public class ConfigController {

    @Setter
    @Autowired
    ConfigService configService;

    @Setter
    @Autowired
    TagService tagService;

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON)
    public Wrapper edit(@RequestBody Map<String, Object> configMap) {
        if (configMap == null) {
            return FollowException.MISS_BODY.get();
        }
        if (configMap.get("type") == null) {
            return FollowException.MISS_TYPE.get();
        } else {
            //判断type合法性
            Integer type = Integer.parseInt(configMap.get("type").toString());
            if (!(RiskEventConfig.RiskEventType.contains(type)
                    || MacroEventConfig.MacroEventType.contains(type)
                    || MarketEventConfig.MarketEventType.contains(type)
                    || ConductConfig.ConductType.contains(type))) {
                return FollowException.WRONG_TYPE.get();
            }
        }
        configMap.putIfAbsent("enable", 1);
        configService.saveOrUpdate(configMap);
        return Wrapper.OK;
    }

    @RequestMapping(value = "/default", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    public Wrapper defaultConfig(@RequestParam("type_code") Integer type){
        if (!(RiskEventConfig.RiskEventType.contains(type)
                || MacroEventConfig.MacroEventType.contains(type)
                || MarketEventConfig.MarketEventType.contains(type)
                || ConductConfig.ConductType.contains(type))) {
            return FollowException.WRONG_TYPE.get();
        }
        AbstractConfig config = configService.getDefault(type);
        return Wrapper.OKBuilder.data(config).build();
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    public Wrapper getList(@RequestParam("type") String type,
                           @RequestParam(value = "offset", required = false) Integer offset,
                           @RequestParam(value = "count", required = false) Integer count) {
        if (!ConfigType.contains(type)) {
            return FollowException.WRONG_TYPE.get();
        }
        ConfigType configType = ConfigType.valueOf(type.toUpperCase());
        Long userId = DefaultSecurityContext.getUserId();
        List<AbstractConfig> data = configService.get(userId, configType, offset, count);
        return Wrapper.OKBuilder.data(data).build();
    }

    /**
     * 推荐关键词 TODO 基于一定算法
     *
     * @param count
     * @return
     */
    @RequestMapping(value = "/suggest", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper getSuggest(@RequestParam(value = "count", required = false, defaultValue = "10") Integer count) {
        Map<String, List<Tag>> data = tagService.getHot(TagLevelOneType.ALL, null, count);
        return Wrapper.OKBuilder.data(data).build();
    }

    /**
     * 搜索关键词
     * @param keyword
     * @param type
     * @param count
     * @return
     */
    @RequestMapping(value = "/keywords", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper getKeywords(@RequestParam(value = "keyword") String keyword,
                               @RequestParam(value = "type") String type,
                               @RequestParam(value = "count", required = false, defaultValue = "10") Integer count) {
        TagLevelOneType levelOneType = TagLevelOneType.get(type);
        if(levelOneType == null){
            return FollowException.WRONG_TYPE.get();
        }
        Map<String, List<Tag>> data = tagService.search(levelOneType, keyword, count);
        return Wrapper.OKBuilder.data(data).build();
    }

    @RequestMapping(value = "/all_category", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper getKeywords(@RequestParam(value = "type") String type) {
        TagLevelOneType levelOneType = TagLevelOneType.get(type);
        if(levelOneType == null){
            return FollowException.WRONG_TYPE.get();
        }
        List<TagFamily> data = tagService.getAllTag(levelOneType);
        return Wrapper.OKBuilder.data(data).build();
    }

    /**
     * 获取宏观事件展示的信息列表
     * @param type
     * @param count
     * @return
     */
    @RequestMapping(value = "/match_infos", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper getMatchingNews(@RequestParam(value = "type") String type,
                                   @RequestParam(value = "offset", defaultValue = "0") Integer offset,
                                   @RequestParam(value = "count", defaultValue = "10") Integer count,
                                   @RequestParam(value = "is_all") boolean isAll
                                  ) {
        if(Strings.isNullOrEmpty(type))
            return FollowException.WRONG_TYPE.get();
        return Wrapper.OKBuilder.data(configService.getMatchingMacroInfos(type, offset, count, isAll)).build();
    }

    @RequestMapping(value = "/match_companys", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper getMatchingCompanys(@RequestParam(value = "macro_id") String macroId,
                                       @RequestParam(value = "type") String type,
                                       @RequestParam(value = "offset") Integer offset,
                                       @RequestParam(value = "count") Integer count
                                  ) {

        return Wrapper.OKBuilder.data(configService.getMatchingCompanies(macroId, type, offset, count)).build();
    }

    @RequestMapping(value = "/macro/store", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public Wrapper storeMacro(@RequestParam(value = "type") String type,
                              @RequestParam(value = "is_store") Boolean isStore,
                              @RequestParam(value = "macro_id") String macroId) {
        if(type == null || isStore == null)
            return Wrapper.ERRORBuilder.msg("param lost").build();
        Long userId = DefaultSecurityContext.getUserId();
        boolean storeStatus = configService.storeMacro(userId,
                MacroEventConfig.MacroEventType.getCodeByTypeEnName(type),
                isStore,
                macroId);
        return Wrapper.OKBuilder.data(storeStatus).build();
    }

    @RequestMapping(value = "/macro/stores", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    public Wrapper getMacroStores(@RequestParam(value = "type") String type,
                                  @RequestParam(value = "offset") Integer offset,
                                  @RequestParam(value = "count") Integer count) {
        if(type == null)
            return Wrapper.ERRORBuilder.msg("param lost").build();
        Long userId = DefaultSecurityContext.getUserId();
        Map macroStores = configService.getMacroStores(userId, MacroEventConfig.MacroEventType.getCodeByTypeEnName(type), offset, count);
        return Wrapper.OKBuilder.data(macroStores).build();
    }

    @RequestMapping(value = "/chance_sea", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper getChanceSea(@RequestParam(value = "count", required = false, defaultValue = "10") Integer limit,
                               @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset,
                               @RequestParam(value = "collected", required = false) Boolean collected) {
        try {
            PageInfo pageInfo = this.configService.getChanceSeaInfoPage(limit, offset, collected);
            return Wrapper.OKBuilder.data(pageInfo).build();
        } catch (Exception e) {
            return Wrapper.ERRORBuilder.msg(e.getMessage()).build();
        }
    }

    @RequestMapping(value = "/chance_sea/detail", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper getChanceSea(@RequestParam(value = "id") Long id) {

        ChanceSeaInfo info = this.configService.getChanceSeaInfoDetail(id);

        try {
            return Wrapper.OKBuilder.data(info).build();
        } catch (Exception e) {
            return Wrapper.ERRORBuilder.msg(e.getMessage()).build();
        }
    }

    @RequestMapping(value = "/chance_sea/collected", method = RequestMethod.PUT)
    public Wrapper storeChanceSea(@RequestParam(value = "id") Long id,
                                  @RequestParam(value = "collected", defaultValue = "true") Boolean collected) {
        if (id == null) {
            return Wrapper.ERRORBuilder.msg("param lost").build();
        }
        try {
            configService.changeChanceSeaStatus(id, collected);
            return Wrapper.OKBuilder.build();
        } catch (Exception e) {
            return Wrapper.ERRORBuilder.data(e.getMessage()).build();
        }

    }

}
