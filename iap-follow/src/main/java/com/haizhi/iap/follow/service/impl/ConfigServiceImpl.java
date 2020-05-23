package com.haizhi.iap.follow.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.haizhi.iap.common.auth.DefaultSecurityContext;
import com.haizhi.iap.common.exception.ServiceAccessException;
import com.haizhi.iap.common.utils.PageUtil;
import com.haizhi.iap.follow.controller.InnerTagSearchWS;
import com.haizhi.iap.follow.controller.NotificationWS;
import com.haizhi.iap.follow.controller.model.*;
import com.haizhi.iap.follow.exception.FollowException;
import com.haizhi.iap.follow.model.*;
import com.haizhi.iap.follow.model.config.AbstractConfig;
import com.haizhi.iap.follow.model.config.ConfigType;
import com.haizhi.iap.follow.model.config.event.macro.AreaPolicyConfig;
import com.haizhi.iap.follow.model.config.event.macro.BiddingDocConfig;
import com.haizhi.iap.follow.model.config.event.macro.IndustryNewsConfig;
import com.haizhi.iap.follow.model.config.event.macro.MacroEventConfig;
import com.haizhi.iap.follow.model.config.event.market.MarketEventConfig;
import com.haizhi.iap.follow.model.config.event.risk.RiskEventConfig;
import com.haizhi.iap.follow.model.config.rule.conduct.ConductConfig;
import com.haizhi.iap.follow.repo.ConfigRepo;
import com.haizhi.iap.follow.repo.MacroStoreRepo;
import com.haizhi.iap.follow.repo.MongoRepo;
import com.haizhi.iap.follow.repo.TagRepo;
import com.haizhi.iap.follow.service.ConfigService;
import com.haizhi.iap.follow.service.NPService;
import com.haizhi.iap.follow.utils.DateUtils;
import com.mongodb.client.MongoCursor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by chenbo on 2017/12/12.
 */
@Slf4j
@Service
public class ConfigServiceImpl implements ConfigService {

    @Setter
    @Autowired
    ConfigRepo configRepo;

    @Setter
    @Autowired
    TagRepo tagRepo;

    @Setter
    @Autowired
    MongoRepo mongoRepo;

    @Setter
    @Autowired
    NotificationWS notificationWS;

    @Setter
    @Autowired
    MacroStoreRepo macroStoreRepo;

    @Setter
    @Autowired
    NPService npService;

    @Setter
    @Autowired
    InnerTagSearchWS innerTagSearchWS;

    private static final int MAX_CONTENT_LENGTH = 5000;
    public static String ENTERPRISE_DATA_GOV = "enterprise_data_gov";

    @Override
    public List<AbstractConfig> get(Long userId, ConfigType configType, Integer offset, Integer count) {
        List<AbstractConfig> configList = Lists.newArrayList();
        switch (configType) {
            case RISK:
                configList = getRisk(userId);
                break;
            case MARKET:
                configList = getMarket(userId);
                break;
            case MACRO:
                configList = getMacro(userId);
                break;
            case CONDUCT:
                configList = getConduct(userId);
                break;
        }
        Collections.sort(configList, Comparator.comparing(AbstractConfig::getName));
        if (offset != null && count != null) {
            return PageUtil.pageList(configList, offset, count);
        } else {
            return configList;
        }
    }

    @Override
    public List<AbstractConfig> getRisk(Long userId) {
        List<AbstractConfig> dbConfigList = configRepo.findRisk(userId);
        List<Integer> allType = RiskEventConfig.RiskEventType.allCode();
        List<Integer> contains = Lists.newArrayList();
        for (AbstractConfig config : dbConfigList) {
            contains.add(config.getType());
        }
        allType.removeAll(contains);

        return merge(allType, dbConfigList);
    }

    @Override
    public List<AbstractConfig> getMarket(Long userId) {
        List<AbstractConfig> dbConfigList = configRepo.findMarket(userId);
        List<Integer> allType = MarketEventConfig.MarketEventType.allCode();
        List<Integer> contains = Lists.newArrayList();
        for (AbstractConfig config : dbConfigList) {
            contains.add(config.getType());
        }
        allType.removeAll(contains);

        return merge(allType, dbConfigList);
    }

    @Override
    public List<AbstractConfig> getMacro(Long userId) {
        List<AbstractConfig> dbConfigList = configRepo.findMacro(userId);

        List<Integer> allType = MacroEventConfig.MacroEventType.allCode();
        List<Integer> contains = Lists.newArrayList();
        for (AbstractConfig config : dbConfigList) {
            contains.add(config.getType());
        }
        allType.removeAll(contains);

        List<AbstractConfig> result = merge(allType, dbConfigList);

        for (AbstractConfig config : result) {
            //补齐默认值
            if (config instanceof MacroEventConfig) {
                processMacroDefault(config);
            }
        }
        return result;
    }

    private AbstractConfig processMacroDefault(AbstractConfig config) {
        List<Tag> tagList = Lists.newArrayList();
        if (((MacroEventConfig) config).getKeywordIds().size() < 1 && config.getId() == null) {
            if (config instanceof AreaPolicyConfig) {
                tagList.addAll(tagRepo.getArea("深圳", null, null, null));
                tagList.addAll(tagRepo.getArea("兰州", null, null, null));
            } else if (config instanceof BiddingDocConfig) {
                //TODO 默认值
            } else if (config instanceof IndustryNewsConfig) {
                tagList.addAll(tagRepo.getIndustry("保险业", null, null, null));
            }
        } else {
            for (Number id : ((MacroEventConfig) config).getKeywordIds()) {
                try {
                    Tag tag = tagRepo.getById(Long.parseLong(id.toString()));
                    if (tag != null) {
                        tagList.add(tag);
                    }
                } catch (NumberFormatException ex) {
                    log.error("存在脏数据，keyword_id:{}", id);
                }
            }
        }
        ((MacroEventConfig) config).setKeywords(tagList);
        return config;
    }

    @Override
    public MacroEventConfig getMacroByEventType(Long userId, Integer eventType) {
        AbstractConfig config = configRepo.findMacroByType(userId, eventType);
        if (config == null)
            return null;
        List<Tag> tagList = Lists.newArrayList();
        for (Number id : ((MacroEventConfig) config).getKeywordIds()) {
            try {
                Tag tag = tagRepo.getById(Long.parseLong(id.toString()));
                if (tag != null) {
                    tagList.add(tag);
                }
            } catch (NumberFormatException ex) {
                log.error("存在脏数据，keyword_id:{}", id);
            }
        }
        ((MacroEventConfig) config).setKeywords(tagList);
        return (MacroEventConfig) config;
    }

    @Override
    public List<AbstractConfig> getConduct(Long userId) {
        List<AbstractConfig> dbConfigList = configRepo.findConduct(userId);
        List<Integer> allType = ConductConfig.ConductType.allCode();
        List<Integer> contains = Lists.newArrayList();
        for (AbstractConfig config : dbConfigList) {
            contains.add(config.getType());
        }
        allType.removeAll(contains);

        return merge(allType, dbConfigList);
    }

    @Override
    public void saveOrUpdate(Map<String, Object> configMap) {
        AbstractConfig config = configRepo.getConfigInstance((Integer) configMap.get("type"));
        config.setUserId(DefaultSecurityContext.getUserId());
        config.setEnable((Integer) configMap.get("enable"));
        config.setParam(configMap);
        //宏观关键词个数限制
        if (config instanceof MacroEventConfig) {
            if (((MacroEventConfig) config).getKeywordIds().size() > 10) {
                throw new ServiceAccessException(FollowException.OVER_LIMIT_KEYWORD);
            }
        }
        if (configMap.get("id") != null) {
            Long id = Long.parseLong(configMap.get("id").toString());
            config.setId(id);
            configRepo.update(config);
        } else {
            configRepo.save(config);
        }
    }

    @Override
    public AbstractConfig getDefault(Integer type) {
        AbstractConfig config = configRepo.getConfigInstance(type);
        if (config instanceof MacroEventConfig) {
            processMacroDefault(config);
        }
        return config;
    }

    @Override
    public Map getMatchingMacroInfos(String type, Integer offset, Integer count, boolean isAll) {
        Long userId = DefaultSecurityContext.getUserId();
        Integer eventType = MacroEventConfig.MacroEventType.getCodeByTypeEnName(type);
        MacroEventConfig configs = getMacroByEventType(userId, eventType);

        List<String> newsId = Lists.newArrayList();
        Object total = null;
        if (configs == null || configs.getKeywordIds().size() == 0) {//by score
            List<NPScore> npScores = npService.findByType(offset, count, type, isAll ? null : DateUtils.getLatestDate());
            npScores.stream().forEach(npScore -> {
                newsId.add(npScore.getMacroId());
            });
            total = npService.getCountByType(type, isAll ? null : DateUtils.getLatestDate());
        } else {//by config
            try {
                InternalWrapper internalWrapper = innerTagSearchWS.searchES(buildSearchParams(configs, offset, count, type, isAll));
                if (internalWrapper != null && internalWrapper.getStatus().equals(0) && internalWrapper.getData() instanceof Map) {
                    Map data = (Map) internalWrapper.getData();
                    if (data.get("docs") == null)
                        return Collections.emptyMap();
                    for (Object d : (List) data.get("docs")) {
                        Map item = (Map) d;
                        newsId.add((String) item.get("id"));
                    }
                    total = data.get("totalHit");
                } else {
                    log.error("getMatchingMacroInfos by config, result: {}", internalWrapper);
                }
            } catch (Exception e) {
                log.error("getMatchingMacroInfos by config error:{}", e);
            }
        }

        List data = null;
        if (newsId.size() > 0) {
            if (type.equals(MacroEventConfig.MacroEventType.INDUSTRY_NEWS.getTypeEnName()))
                data = getMacroIndustryNewsInfos(newsId);
            else
                data = getBidInfos(newsId);
        }

        Map<String, Object> result = Maps.newHashMap();
        result.put("data", data);
        result.put("total_count", total);

        return result;
    }

    private TagDetailSearchRequest buildSearchParams(MacroEventConfig configs, int offset, int count, String type, boolean isAll) {
        List<TagDetailRequest> tagDetailRequests = Lists.newArrayList();
        configs.getKeywords().stream().forEach(tag -> {
            TagDetailRequest tagDetailRequest = new TagDetailRequest();
            tagDetailRequest.setFieldName(tag.getFieldName());
            Map<String, Object> op = Maps.newHashMap();
            op.put("eq", tag.getId().toString());
            tagDetailRequest.setOp(op);
            tagDetailRequests.add(tagDetailRequest);
        });

        TagDetailSearchRequest tagDetailSearchRequest = new TagDetailSearchRequest();
        tagDetailSearchRequest.setFrom(offset);
        tagDetailSearchRequest.setSize(count);
        tagDetailSearchRequest.setEsIndexName(StringUtils.join("tag", "_", type));
        tagDetailSearchRequest.setSearchParams(tagDetailRequests);
        tagDetailSearchRequest.setOrderBy("utime_l");
        if (!isAll) {//for test
/*            List<Map<String, Object>> filters = Lists.newArrayList();
            Map<String, Object> filter = Maps.newHashMap();
            filter.put("field", "utime_l");
            filter.put("min", DateUtils.getLatestDate());
            filters.add(filter);
            tagDetailSearchRequest.setFilters(filters);*/
        }

        return tagDetailSearchRequest;
    }

    private List<MacroNewsInfo> getMacroIndustryNewsInfos(List<String> ids) {
        Long userId = DefaultSecurityContext.getUserId();
        List<MacroNewsInfo> macroNewsInfos = Lists.newArrayList();

        MongoCursor<Document> cursor = mongoRepo.getByIds(MacroEventConfig.MacroEventType.INDUSTRY_NEWS.getTypeEnName(), ids);
        Map<String, MacroNewsInfo> result = Maps.newHashMap();
        while (cursor.hasNext()) {
            Document doc = cursor.next();
            MacroNewsInfo macroNewsInfo = new MacroNewsInfo();
            if (doc.get("title") != null)
                macroNewsInfo.setTitle((String) doc.get("title"));
            if (doc.get("sentiment") != null)
                macroNewsInfo.setSentiment((String) doc.get("sentiment"));
            if (doc.get("_utime") != null)
                macroNewsInfo.setDate((String) doc.get("_utime"));
            macroNewsInfo.setMacroId(doc.get("_id").toString());
            macroNewsInfo.setStore(macroStoreRepo.isStore(userId, MacroEventConfig.MacroEventType.INDUSTRY_NEWS.getCode(), doc.get("_id").toString()));

            if (doc.get("summary") != null)
                macroNewsInfo.setKeyWords(dealParticiple((String) doc.get("summary"), false));
            result.put(macroNewsInfo.getMacroId(), macroNewsInfo);
        }
        for (String id : ids) {
            macroNewsInfos.add(result.get(id));
        }
        return macroNewsInfos;
    }


    private List<MacroBidInfo> getBidInfos(List<String> ids) {
        Long userId = DefaultSecurityContext.getUserId();
        List<MacroBidInfo> macroBidInfos = Lists.newArrayList();

        MongoCursor<Document> cursor = mongoRepo.getByIds(MacroEventConfig.MacroEventType.BIDDING_DOC.getTypeEnName(), ids);
        Map<String, MacroBidInfo> result = Maps.newHashMap();
        while (cursor.hasNext()) {
            Document doc = cursor.next();
            MacroBidInfo macroBidInfo = new MacroBidInfo();
            if (doc.get("title") != null)
                macroBidInfo.setTitle((String) doc.get("title"));
            if (doc.get("city") != null)
                macroBidInfo.setDistrict((String) doc.get("city"));
            if (doc.get("publish_time") != null)
                macroBidInfo.setDate((String) doc.get("publish_time"));
            if (doc.get("public_bid_company") != null)
                macroBidInfo.setBidCompanys((List) doc.get("public_bid_company"));
            macroBidInfo.setMacroId(doc.get("_id").toString());
            macroBidInfo.setStore(macroStoreRepo.isStore(userId, MacroEventConfig.MacroEventType.BIDDING_DOC.getCode(), doc.get("_id").toString()));

            if (doc.get("content") != null) {
                try {
                    macroBidInfo.setKeyWords(dealParticiple((String) doc.get("content"), false));
                } catch (ClassCastException e) {
                    log.error("{}", e.getMessage());
                }
            }
            result.put(macroBidInfo.getMacroId(), macroBidInfo);
        }

        for (String id : ids)
            macroBidInfos.add(result.get(id));
        return macroBidInfos;
    }

    private String dealParticiple(String content, boolean needParentName) {
        TagParticipleParam tagParticipleParam = new TagParticipleParam();
        content = StringUtils.trimToEmpty(content);
        if (content.length() > MAX_CONTENT_LENGTH) {
            content = content.substring(0, MAX_CONTENT_LENGTH);
        }
        tagParticipleParam.setContent(content);
        if (needParentName) {
            tagParticipleParam.setR_tag_pnames(true);
        }
        Set<String> tagNames = Sets.newHashSet();

        try {
            InternalWrapper internalWrapper = innerTagSearchWS.findTagFromContent(tagParticipleParam);
            if (internalWrapper != null && internalWrapper.getStatus().equals(0)
                    && internalWrapper.getData() instanceof List) {
                List data = (List) internalWrapper.getData();
                data.stream().forEach(d -> {
                    Map item = (Map) d;
                    tagNames.add((String) item.get("name"));
                    if (needParentName && item.get("parent_names") != null) {
                        List<String> pnames = (List<String>) item.get("parent_names");
                        for (int i = pnames.size() - 1; i >= 0; i--) {
                            if (pnames.size() - i > 0) {
                                break;  // 0代表不要父亲，1代表1个父亲...邹彦说暂时不要了
                            }
                            tagNames.add(pnames.get(i));
                        }
                    }
                });
            } else {
                log.error("buildSearchParams invoke findTagFromContent, result: {}", internalWrapper);
            }
        } catch (Exception e) {
            log.error("dealParticiple invoke findTagFromContent error:{}", e);
        }

        return StringUtils.join(tagNames, "、");
    }

    @Override
    public Map getMatchingCompanies(String macroId, String type, Integer offset, Integer count) {
        if (MacroEventConfig.MacroEventType.INDUSTRY_NEWS.getTypeEnName().equals(type)) {
            return getMatchIndustryNewsCompanies(macroId, offset, count);
        } else if (MacroEventConfig.MacroEventType.BIDDING_DOC.getTypeEnName().equals(type)) {
            return getMatchBidCompanies(macroId, offset, count);
        }
        return null;
    }

    private Map getMatchIndustryNewsCompanies(String newsId, Integer offset, Integer count) {
        Map<String, Object> data = Maps.newHashMap();
        MongoCursor<Document> docs = mongoRepo.getById(MacroEventConfig.MacroEventType.INDUSTRY_NEWS.getTypeEnName(), newsId);
        if (docs.hasNext()) {
            Document doc = docs.next();
            data.put("summary", doc.get("summary"));
            data.put("source", doc.get("source"));
            data.put("mainbody_source", doc.get("mainbody_source"));
        }
        String newsKeyWords = data.get("summary") == null ? "" : dealParticiple((String) data.get("summary"), true);

        try {
            InternalWrapper internalCompanyWrapper = innerTagSearchWS.searchES(buildSearchParams((String) data.get("summary"), offset, count, true));
            if (internalCompanyWrapper != null && internalCompanyWrapper.getStatus().equals(0)
                    && internalCompanyWrapper.getData() instanceof Map) {
                int total = (Integer) (((Map) internalCompanyWrapper.getData()).get("totalHit"));
                if(total > 0l) {
                    List<MacroCompanyInfo> macroCompanysInfos = getMacroCompanysInfo(newsKeyWords, internalCompanyWrapper);
                    data.put("companys", macroCompanysInfos);
                    data.put("total_count", total);
                } else {
                    try {
                        InternalWrapper internalNHCompanyWrapper = innerTagSearchWS.searchES(buildSearchParams((String) data.get("summary"), offset, count, false));
                        if (internalNHCompanyWrapper != null && internalNHCompanyWrapper.getStatus().equals(0)
                                && internalNHCompanyWrapper.getData() instanceof Map) {
                            List<MacroCompanyInfo> macroNHCompanysInfos = getMacroCompanysInfo(newsKeyWords, internalNHCompanyWrapper);
                            data.put("companys", macroNHCompanysInfos);
                            data.put("total_count", ((Map) internalNHCompanyWrapper.getData()).get("totalHit"));
                        } else {
                            log.error("getMatchingMacroInfos by config, result: {}", internalNHCompanyWrapper);
                        }
                    } catch (Exception e) {
                        log.error("getMatchIndustryNewsCompanies invoke searchES error:{}", e);
                    }
                }
            } else {
                log.error("getMatchingMacroInfos by config, result: {}", internalCompanyWrapper);
            }
        } catch (Exception e) {
            log.error("getMatchIndustryNewsCompanies invoke searchES error:{}", e);
        }
        return data;
    }

    private Map getMatchBidCompanies(String bidId, Integer offset, Integer count) {
        Map<String, Object> data = Maps.newHashMap();
        MongoCursor<Document> docs = mongoRepo.getById(MacroEventConfig.MacroEventType.BIDDING_DOC.getTypeEnName(), bidId);
        Object recommendCompany = null;
        while (docs.hasNext()) {
            Document doc = docs.next();
            data.put("content", doc.get("content"));
            recommendCompany = doc.get("recommend_company");
            data.put("mainbody_source", doc.get("mainbody_source"));
            break;
        }
        String bidKeyWords = dealParticiple((String) data.get("content"), false);
        if (recommendCompany != null && recommendCompany instanceof List) {
            List companyNames = (List) recommendCompany;
            List<MacroCompanyInfo> macroCompanysInfos = getMacroCompanysInfo(bidKeyWords, companyNames, offset, count);
            data.put("companys", macroCompanysInfos);
            data.put("total_count", companyNames == null ? 0 : companyNames.size());
        }
        return data;
    }

    private TagDetailSearchRequest buildSearchParams(String text, int offset, int count, boolean is_hangnei) {
        TagParticipleParam tagParticipleParam = new TagParticipleParam();
        if (text.length() > MAX_CONTENT_LENGTH) {
            text = text.substring(0, MAX_CONTENT_LENGTH);
        }
        tagParticipleParam.setContent(text);
        List<TagDetailRequest> tagDetailRequests = Lists.newArrayList();
        try {
            InternalWrapper internalWrapper = innerTagSearchWS.findTagFromContent(tagParticipleParam);

            if (internalWrapper != null && internalWrapper.getStatus().equals(0)
                    && internalWrapper.getData() instanceof List) {
                List data = (List) internalWrapper.getData();

                data.stream().forEach(tag -> {
                    Map item = (Map) tag;
                    TagDetailRequest tagDetailRequest = new TagDetailRequest();
                    tagDetailRequest.setFieldName((String) item.get("field_name"));
                    Map<String, Object> op = Maps.newHashMap();
                    op.put("eq", item.get("id").toString());
                    tagDetailRequest.setOp(op);
                    tagDetailRequests.add(tagDetailRequest);
                });
            } else {
                log.error("buildSearchParams invoke findTagFromContent, result: {}", internalWrapper);
            }
        } catch (Exception e) {
            log.error("buildSearchParams invoke findTagFromContent error:{}", e);
        }

        TagDetailSearchRequest tagDetailSearchRequest = new TagDetailSearchRequest();
        tagDetailSearchRequest.setFrom(offset);
        tagDetailSearchRequest.setSize(count);
        tagDetailSearchRequest.setEsIndexName(StringUtils.join("tag_", ENTERPRISE_DATA_GOV));
        tagDetailSearchRequest.setSearchParams(tagDetailRequests);
        tagDetailSearchRequest.setOrderBy("utime_l");
        if(is_hangnei) {
            List<Map<String, Object>> filters = Lists.newArrayList();
            Map<String, Object> filter = Maps.newHashMap();
            filter.put("field", "is_hangnei_i");
            filter.put("eq", 1);
            filters.add(filter);
            tagDetailSearchRequest.setFilters(filters);
        }
        return tagDetailSearchRequest;
    }

    private List<MacroCompanyInfo> getMacroCompanysInfo(String newsKeyWords, InternalWrapper internalWrapper) {
        List<MacroCompanyInfo> macroCompanysInfos = Lists.newArrayList();
        Map data = (Map) internalWrapper.getData();
        if (data.get("docs") == null)
            return macroCompanysInfos;
        for (Object d : (List) data.get("docs")) {
            Map item = (Map) d;
            MacroCompanyInfo macroCompanyInfo = getCompanyDetail((String) item.get("id"), newsKeyWords);
            macroCompanysInfos.add(macroCompanyInfo);
        }
        return macroCompanysInfos;
    }

    private MacroCompanyInfo getCompanyDetail(String id, String newsKeyWords) {
        MongoCursor<Document> cursor = mongoRepo.getById(ENTERPRISE_DATA_GOV, id);
        MacroCompanyInfo macroCompanyInfo = null;
        if (cursor.hasNext()) {
            Document doc = cursor.next();
            macroCompanyInfo = getCompanyInfo(doc, newsKeyWords);
        }

        return macroCompanyInfo;
    }

    private String crossKeyWords(String[] keywords1, String[] keywords2) {
        Set<String> result = Sets.newHashSet();
        for (String k1 : keywords1) {
            for (String k2 : keywords2) {
                if (k2.equals(k1)) {
                    result.add(k1);
                }
            }
        }
        return StringUtils.join(result, "、");
    }

    private List<MacroCompanyInfo> getMacroCompanysInfo(String bidKeyWords, List<String> companyNames, Integer offset, Integer count) {
        List<MacroCompanyInfo> macroCompanysInfos = Lists.newArrayList();
        MongoCursor<Document> cursor = mongoRepo.getByFields(ENTERPRISE_DATA_GOV, "company", companyNames);
        MacroCompanyInfo macroCompanyInfo = null;
        int cal = -1;

        while (cursor.hasNext()) {
            Document doc = cursor.next();
            cal++;
            if (cal < offset)
                continue;
            if (cal > (count + offset - 1))
                break;
            macroCompanyInfo = getCompanyInfo(doc, bidKeyWords);
            macroCompanysInfos.add(macroCompanyInfo);
        }
        return macroCompanysInfos;
    }

    private MacroCompanyInfo getCompanyInfo(Document doc, String keyWords) {
        MacroCompanyInfo macroCompanyInfo = new MacroCompanyInfo();
        macroCompanyInfo.setId(doc.get("_id").toString());
        macroCompanyInfo.setName(doc.get("company").toString());
        if (doc.get("business_scope") != null) {
            String companyKeywords = dealParticiple(doc.get("business_scope").toString(), true);
            macroCompanyInfo.setKeyWords(crossKeyWords(StringUtils.split(companyKeywords, "、"), StringUtils.split(keyWords, "、")));
        }

        Object relationData = null;
        Object relation = null;
        Object company = null;
        try {
            relationData = ((Map) notificationWS.getRlationship(doc.get("company").toString())).get("data");
            if (relationData != null && ((Map) relationData).get("company") != null) {
                company = ((Map) relationData).get("company");
            }
            if (relationData != null && ((Map) relationData).get("relationship") != null) {
                relation = ((Map) relationData).get("relationship");
            }
        } catch (Exception e) {
            log.error("notification getRlationship error.");
        }
        macroCompanyInfo.setCompany(company == null ? null : company.toString());
        macroCompanyInfo.setRelation(relation == null ? null : relation.toString());

        return macroCompanyInfo;
    }

    @Override
    public boolean storeMacro(Long userId, Integer type, boolean isStore, String macroId) {
        if (isStore) {
            return macroStoreRepo.create(userId, type, macroId);
        } else {
            return macroStoreRepo.delete(userId, type, macroId);
        }
    }

    @Override
    public Map getMacroStores(Long userId, Integer type, Integer offset, Integer count) {
        List<String> macroIds = macroStoreRepo.getStores(userId, type, offset, count);
        Map<String, Object> result = Maps.newHashMap();
        if (MacroEventConfig.MacroEventType.INDUSTRY_NEWS.getCode().equals(type)) {
            result.put("data", getMacroIndustryNewsInfos(macroIds));
        } else if (MacroEventConfig.MacroEventType.BIDDING_DOC.getCode().equals(type)) {
            result.put("data", getBidInfos(macroIds));
        }
        result.put("total_count", macroStoreRepo.getCount(userId, type));
        return result;
    }

    private List<AbstractConfig> merge(List<Integer> notContains, List<AbstractConfig> dbConfigList) {
        List<AbstractConfig> result = Lists.newArrayList();
        for (Integer notContainType : notContains) {
            AbstractConfig defaultConfig = configRepo.getConfigInstance(notContainType);

            if (defaultConfig == null) {
                log.warn("type {}'s instance is null", notContainType);
            } else {
                result.add(defaultConfig);
            }
        }
        result.addAll(dbConfigList);
        return result;
    }

    @Override
    public PageInfo getChanceSeaInfoPage(Integer limit, Integer offset, Boolean collected) {

        List<ChanceSeaInfo> data = null;
        Long count = null;
        PageInfo pageInfo = null;
        Long userID = DefaultSecurityContext.getUserId();

        if (collected == null || collected == false) {
            data = this.configRepo.getChanceSeaInfoList(userID, limit, offset);
            count = this.configRepo.getChanceSeaInfoCount();
            pageInfo = new PageInfo(data, count);
        } else {
            List<Long> idList = this.configRepo.getChanceSeaCollectedIDList(userID, limit, offset);
            count = this.configRepo.getChanceSeaCollectedCount(userID);
            if (idList != null && !idList.isEmpty()) {
                data = this.configRepo.getChanceSeaCollectedList(idList);
            }
            pageInfo = new PageInfo(data, count);
        }

        if (data != null) {
            for (ChanceSeaInfo item : data) {
                if (collected != null && collected == true) {
                    item.setCollected(true);
                }
                item.setPushTime(item.getPushTime().split(" ")[0]);
            }
        }

        return pageInfo;
    }

    @Override
    public ChanceSeaInfo getChanceSeaInfoDetail(Long id) {
        return this.configRepo.getChanceSeaInfoDetail(id);
    }

    @Override
    public void changeChanceSeaStatus(Long id, boolean isStore) {
        Long userID = DefaultSecurityContext.getUserId();
        this.configRepo.changeChanceSeaStatus(id, userID, isStore);
    }
}
