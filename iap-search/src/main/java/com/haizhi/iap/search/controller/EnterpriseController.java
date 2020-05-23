package com.haizhi.iap.search.controller;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.haizhi.iap.common.Wrapper;
import com.haizhi.iap.common.auth.DefaultSecurityContext;
import com.haizhi.iap.common.auth.NoneAuthorization;
import com.haizhi.iap.common.utils.MapBuilder;
import com.haizhi.iap.search.constant.EntityType;
import com.haizhi.iap.search.constant.Fields;
import com.haizhi.iap.search.controller.model.AdvancedSearchReq;
import com.haizhi.iap.search.controller.model.DataItem;
import com.haizhi.iap.search.controller.model.ESEnterpriseReq;
import com.haizhi.iap.search.controller.model.EnterpriseReq;
import com.haizhi.iap.search.controller.model2.SearchRequest;
import com.haizhi.iap.search.controller.model2.SearchType;
import com.haizhi.iap.search.enums.ESEnterpriseSearchType;
import com.haizhi.iap.search.enums.EnterpriseSearchType;
import com.haizhi.iap.search.exception.SearchException;
import com.haizhi.iap.search.model.Beneficiary;
import com.haizhi.iap.search.model.BrowsingHistory;
import com.haizhi.iap.search.model.CTag;
import com.haizhi.iap.search.model.qo.GroupMembersSearchQo;
import com.haizhi.iap.search.model.vo.GroupCompanyVo;
import com.haizhi.iap.search.model.CompanyRelate;
import com.haizhi.iap.search.model.DynamicInfo;
import com.haizhi.iap.search.model.IndustryStatistics;
import com.haizhi.iap.search.model.NewRegisteredCompany;
import com.haizhi.iap.search.model.TRANCORP;
import com.haizhi.iap.search.model.Tag;
import com.haizhi.iap.search.model.TagCategory;
import com.haizhi.iap.search.model.vo.GroupMembersVo;
import com.haizhi.iap.search.repo.BeneficiaryRepo;
import com.haizhi.iap.search.repo.CaiBaoCompaniesAbilityRepo;
import com.haizhi.iap.search.repo.CustInfoRepo;
import com.haizhi.iap.search.repo.CustomerTagsRepo;
import com.haizhi.iap.search.repo.ESEnterpriseSearchRepo;
import com.haizhi.iap.search.repo.ESOverviewRepo;
import com.haizhi.iap.search.repo.EnterpriseRepo;
import com.haizhi.iap.search.repo.FinIndexInfoRepo;
import com.haizhi.iap.search.repo.IndustryAverageInfoRepo;
import com.haizhi.iap.search.repo.IndustryStatisticsRepo;
import com.haizhi.iap.search.repo.InnerCompanyLinkOuterRepo;
import com.haizhi.iap.search.repo.NewRegisteredCompanyRepo;
import com.haizhi.iap.search.repo.TRANCORPRepo;
import com.haizhi.iap.search.repo.TagCategoryRepo;
import com.haizhi.iap.search.repo.TagRepo;
import com.haizhi.iap.search.service.BrowsingHistoryService;
import com.haizhi.iap.search.service.CompanyService;
import com.haizhi.iap.search.service.EnterpriseSearchService;
import com.haizhi.iap.search.service.GraphClustersService;
import com.haizhi.iap.search.service.GraphService;
import com.haizhi.iap.search.service.GroupService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.document.AbstractXlsView;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by chenbo on 16/12/23.
 */
@Api(tags="【搜索-企业信息模块】企业相关信息操作")
@Slf4j
@RestController
@RequestMapping(value = "/search")
public class EnterpriseController {

    @Autowired
    TagCategoryRepo tagCategoryRepo;
 
    @Autowired
    TagRepo tagRepo;
    
    @Autowired
    CustomerTagsRepo customerTagsRepo;
    
    @Setter
    @Autowired
    ESEnterpriseSearchRepo esEnterpriseSearchRepo;

    @Setter
    @Autowired
    EnterpriseSearchService enterpriseSearchService;

    @Setter
    @Autowired
    BrowsingHistoryService browsingHistoryService;

    @Setter
    @Autowired
    GraphService graphService;

    @Setter
    @Autowired
    ESOverviewRepo overviewRepo;
    
    @Autowired
    TRANCORPRepo tRANCORPRepo;
    
    @Autowired
    InnerCompanyLinkOuterRepo  innerCompanyLinkOuterRepo;
    
    @Autowired
    NewRegisteredCompanyRepo newRegisteredCompanyRepo;

    @Setter
    @Autowired
    CompanyService companyService;
    
    @Autowired
    private EnterpriseRepo enterpriseRepo;
    
    @Autowired
    private CaiBaoCompaniesAbilityRepo caiBaoCompaniesAbilityRepo;
    
    @Autowired
    private IndustryStatisticsRepo industryStatisticsRepo;
    
    @Autowired
    private IndustryAverageInfoRepo industryAverageInfoRepo;
    
    @Autowired
    private BeneficiaryRepo beneficiaryRepo;
    
    @Autowired
    private CustInfoRepo custInfoRepo;
    
    @Autowired
    private FinIndexInfoRepo finIndexInfoRepo;

    @Autowired
    private GroupService groupService;
    
    @Setter
    @Autowired
    GraphClustersService graphClustersService;

    private static Integer DEFAULT_PAGE_SIZE = 10;

    private static List<String> enterpriseSearchTypes = Lists.newArrayList();

    private static List<String> infoTypeList = Lists.newArrayList("concert", "shareholder", "officer");

    private static List<String> dynamicTypeList = Lists.newArrayList("_all", "risk", "marketing", "unknown");

    @PostConstruct
    private void init() {
        for (EnterpriseSearchType type : EnterpriseSearchType.values()) {
            enterpriseSearchTypes.add(type.getName());
        }
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    public Wrapper search(@RequestParam("name") String name,
                          @RequestParam(value = "stock_code", required = false) String stockCode,
//                   @RequestParam("stock_type") String stockType,
                          @RequestParam("type") String type,
                          @RequestParam(value = "sub_type", required = false) String subType,
                          @RequestParam(value = "third_type", required = false) String thirdType,
                          @RequestParam(value = "year_quarter", required = false) String yearQuarter,
                          @RequestParam(value = "only_count", required = false) Integer onlyCount,
                          @RequestParam(value = "offset", required = false) Integer offset,
                          @RequestParam(value = "count", required = false) Integer count) {
        if (Strings.isNullOrEmpty(type)) {
            return SearchException.MISS_TYPE.get();
        } else if (!enterpriseSearchTypes.contains(type)) {
            return SearchException.WRONG_TYPE.get();
        }

        if (Strings.isNullOrEmpty(name)) {
            return SearchException.MISS_NAME.get();
        }

        if (!Strings.isNullOrEmpty(subType) && subType.equals("news")
                && Strings.isNullOrEmpty(thirdType)) {
            return SearchException.MISS_THIRD_TYPE.get();
        }

        if (onlyCount == null) {
            onlyCount = 1;
        }

        if (offset == null) {
            offset = 0;
        }
        if (count == null) {
            count = DEFAULT_PAGE_SIZE;
        }
        Boolean onlyCounting = onlyCount.equals(1);

        EnterpriseReq req = new EnterpriseReq(name, stockCode, type, subType, thirdType, yearQuarter, onlyCounting, offset, count);

        Object data = enterpriseSearchService.search(req);
        return Wrapper.OKBuilder.data(data).build();
    }


    /**
     * 详情搜索的重排版，企业360视图所有信息(
     * 一个接口包含360视图所有模块的信息，
     * 通过传参可以只查询某一个三级模块，
     * 也可以一次查询所有模块，
     * 也可以只查询所有模块的信息数量，而不要具体的信息)
     */
    @RequestMapping(value = "/v2", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    public Wrapper searchV2(@RequestParam("name") String name,
                          @RequestParam(value = "stock_code", required = false) String stockCode,
                          @RequestParam("type") String type,
                          @RequestParam(value = "sub_type", required = false) String subType,
                          @RequestParam(value = "third_type", required = false) String thirdType,
                          @RequestParam(value = "year_quarter", required = false) String yearQuarter,
                          @RequestParam(value = "only_count", required = false) Integer onlyCount,
                          @RequestParam(value = "offset", required = false) Integer offset,
                          @RequestParam(value = "count", required = false) Integer count) {
        if (Strings.isNullOrEmpty(type)) {
            return SearchException.MISS_TYPE.get();
        } else if (!SearchType.contains(type)) {
            return SearchException.WRONG_TYPE.get();
        }

        if (Strings.isNullOrEmpty(name)) {
            return SearchException.MISS_NAME.get();
        }

        if (onlyCount == null) {
            onlyCount = 1;
        }

        if (offset == null) {
            offset = 0;
        }
        if (count == null) {
            count = DEFAULT_PAGE_SIZE;
        }
        Boolean onlyCounting = onlyCount.equals(1); //是否只需要计数，还是要详细的信息数据

        SearchRequest req = new SearchRequest(name, stockCode, type, subType, thirdType, yearQuarter, onlyCounting, offset, count);

        Object data = companyService.search(req);
        return Wrapper.ok(data);
    }

    /**
     * 详情搜索的重排版，企业360视图所有信息(
     * 一个接口包含360视图所有模块的信息，
     * 通过传参可以只查询某一个三级模块，
     * 也可以一次查询所有模块，
     * 也可以只查询所有模块的信息数量，而不要具体的信息)
     */
    @RequestMapping(value = "/v3", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    public Wrapper searchV3(@RequestParam("name") String name,
                            @RequestParam(value = "stock_code", required = false) String stockCode,
                            @RequestParam("type") String type,
                            @RequestParam(value = "sub_type", required = false) String subType,
                            @RequestParam(value = "third_type", required = false) String thirdType,
                            @RequestParam(value = "year_quarter", required = false) String yearQuarter,
                            @RequestParam(value = "only_count", required = false) Integer onlyCount,
                            @RequestParam(value = "offset", required = false) Integer offset,
                            @RequestParam(value = "count", required = false) Integer count) {
        if (Strings.isNullOrEmpty(type)) {
            return SearchException.MISS_TYPE.get();
        } else if (!SearchType.contains(type)) {
            return SearchException.WRONG_TYPE.get();
        }

        if (Strings.isNullOrEmpty(name)) {
            return SearchException.MISS_NAME.get();
        }

        if (onlyCount == null) {
            onlyCount = 1;
        }

        if (offset == null) {
            offset = 0;
        }
        if (count == null) {
            count = DEFAULT_PAGE_SIZE;
        }
        Boolean onlyCounting = onlyCount.equals(1); //是否只需要计数，还是要详细的信息数据

        SearchRequest req = new SearchRequest(name, stockCode, type, subType, thirdType, yearQuarter, onlyCounting, offset, count);

        Object data = companyService.searchV3(req);
        return Wrapper.ok(data);
    }

    /**
     * (须上报的)公司是否已在库
     *
     * @param company
     */
    @RequestMapping(value = "/is_stored", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper isStored(@RequestParam("company") String company) {
        Map<String, Object> data = Maps.newHashMap();
        data.put("stored", enterpriseSearchService.isStored(company));
        return Wrapper.OKBuilder.data(data).build();
    }

    @RequestMapping(value = "/advanced", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper advancedSearch(@RequestBody AdvancedSearchReq req) throws IOException {
        if (req.getMarketSort() != null) {
            if (req.getMarketSort() > 0) {
                req.setMarketSort(1);
            } else if (req.getMarketSort() == 0) {
                req.setMarketSort(null);
            } else {
                req.setMarketSort(-1);
            }
        }

        if (req.getRiskSort() != null) {
            if (req.getRiskSort() > 0) {
                req.setRiskSort(1);
            } else if (req.getRiskSort() == 0) {
                req.setRiskSort(null);
            } else {
                req.setRiskSort(-1);
            }
        }
        DataItem dataItem = overviewRepo.advancedSearch(req);
        return Wrapper.OKBuilder.data(dataItem).build();
    }

    @RequestMapping(value = "/history", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper history(@RequestParam(value = "offset", required = false) Integer offset,
                           @RequestParam(value = "count", required = false) Integer count) {
        Long userId = DefaultSecurityContext.getUserId();
        if (userId == null) {
            return Wrapper.OK;
        }
        if (offset == null) {
            offset = 0;
        }
        if (count == null) {
            count = 5;
        }
        List<BrowsingHistory> historyList = browsingHistoryService.findByUser(userId, offset, count);
        Map<String, Object> map = Maps.newHashMap();
        map.put("total", browsingHistoryService.countByUser(userId));
        map.put("data", historyList);
        return Wrapper.OKBuilder.data(map).build();
    }

    @RequestMapping(value = "/es_suggest", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    @NoneAuthorization
    public Wrapper esSuggest(@RequestParam("key_word") String keyWord,
                             @RequestParam("type") String type,
                             @RequestParam(value = "count", required = false) Integer count) throws IOException {
        if (Strings.isNullOrEmpty(keyWord)) {
            return SearchException.MISS_KEY_WORD.get();
        }
        Integer suggestSize = 10;
        if (count != null) {
            suggestSize = count > 20 ? 20 : count;
        }

        if (type == null) {
            type = ESEnterpriseSearchType.NAME.getName();
        }

        List<String> suggests = esEnterpriseSearchRepo.suggest(keyWord, type, 100,
                Collections.singletonList(ESEnterpriseSearchType.NAME.getName()), suggestSize);

        return Wrapper.OKBuilder.data(suggests).build();
    }

    /**
     * 360视图搜索企业
     * @param keyWord 关键字
     * @param type 搜索类型
     * @param from
     * @param size
     * @param province 省份
     * @param city 城市
     * @param industry 行业
     * @param registerFoundMin 注册资本
     * @param registerFoundMax 注册资本
     * @param registerDateStart 注册日期
     * @param registerDateEnd 注册日期
     * @return
     */
    @RequestMapping(value = "/es_search", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    @NoneAuthorization
    public Wrapper esSearch(@RequestParam("key_word") String keyWord,
                            @RequestParam("type") String type,
                            @RequestParam(value = "offset", required = false) Integer from,
                            @RequestParam(value = "count", required = false) Integer size,
                            @RequestParam(value = "province", required = false) String province,
                            @RequestParam(value = "city", required = false) String city,
                            @RequestParam(value = "industry", required = false) String industry,
                            @RequestParam(value = "registered_capital_min", required = false) Double registerFoundMin,
                            @RequestParam(value = "registered_capital_max", required = false) Double registerFoundMax,
                            @RequestParam(value = "registered_date_min", required = false) Long registerDateStart,
                            @RequestParam(value = "registered_date_max", required = false) Long registerDateEnd) throws IOException {
        if (Strings.isNullOrEmpty(keyWord)) {
            return SearchException.MISS_KEY_WORD.get();
        }
        if (type == null) {
            type = ESEnterpriseSearchType.NAME.getName();
        }
        Integer searchSize = 10;
        if (size != null) {
            searchSize = size > 20 ? 20 : size;
        }
        if (from == null) {
            from = 0;
        } else if (from > 1000) {
            from = 1000; //最多翻50页
        }
        ESEnterpriseReq req = new ESEnterpriseReq(keyWord, type, province, city, industry, from,
                searchSize, registerFoundMin, registerFoundMax, registerDateStart, registerDateEnd);

        Map<String, Object> result = esEnterpriseSearchRepo.search(req, 500);

        return Wrapper.OKBuilder.data(result).build();
    }

    @RequestMapping(value = "/dynamic_infos", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper dynamicInfo(@RequestParam("company") String company,
                               @RequestParam("type") String type,
                               @RequestParam(value = "offset", required = false) Integer offset,
                               @RequestParam(value = "count", required = false) Integer count) {
        if (Strings.isNullOrEmpty(company)) {
            return SearchException.MISS_COMPANY.get();
        }

        if (Strings.isNullOrEmpty(type)) {
            return SearchException.MISS_TYPE.get();
        } else if (!dynamicTypeList.contains(type)) {
            return SearchException.WRONG_TYPE.get();
        }

        if (offset == null) {
            offset = 0;
        }

        if (count == null) {
            count = 10;
        }

        List<DynamicInfo> result = enterpriseSearchService.getDynamicInfo(company, type);

        DataItem data = DataItem.builder()
                .data(pageList(result, offset, count))
                .totalCount((long) result.size())
                .build();

        return Wrapper.OKBuilder.data(data).build();
    }

    @RequestMapping(value = "/tag", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper tagForCompany(@RequestParam("company") String company) {
        if (Strings.isNullOrEmpty(company)) {
            return SearchException.MISS_COMPANY.get();
        }
        Map<String, Object> result = overviewRepo.searchTag(company);
        result.put("enterprise_tax_rank", enterpriseSearchService.getTaxRank(company));

        return Wrapper.OKBuilder.data(result).build();
    }

    @RequestMapping(value = "/faction", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    public Wrapper groupForCompany(@RequestParam("company") String company) {
        if (Strings.isNullOrEmpty(company)) {
            return SearchException.MISS_COMPANY.get();
        }
        Map<String, Object> result = Maps.newHashMap();
        Pair<String, String> faction = enterpriseSearchService.getFaction(company);
        result.put("faction_id", faction.getLeft());
        result.put("faction", faction.getRight());
        return Wrapper.OKBuilder.data(result).build();
    }

    /**
     * 从图谱中获取 一致行动人,股东对外投资及任职,高管对外投资及任职
     */
    @RequestMapping(value = "/graph_info", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    public Wrapper graphInfo(@RequestParam("company") String company,
                             @RequestParam("info_type") String infoType) {
        if (Strings.isNullOrEmpty(company)) {
            return SearchException.MISS_COMPANY.get();
        }
        if (Strings.isNullOrEmpty(infoType) || !infoTypeList.contains(infoType)) {
            return SearchException.WRONG_INFO_TYPE.get();
        }
        Object data = null;
        if (infoType.equals(infoTypeList.get(0))) {
            data = enterpriseSearchService.getGraphConcert(company);
        } else if (infoType.equals(infoTypeList.get(1))) {
            data = enterpriseSearchService.getGraphContributor(company);
        } else if (infoType.equals(infoTypeList.get(2))) {
            data = enterpriseSearchService.getGraphKeyPerson(company);
        }
        return Wrapper.OKBuilder.data(new MapBuilder().put("list", data).build()).build();
    }

    /**
     * 获取汇率表
     */
    @RequestMapping(value = "/exchange_rate_map", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper getExchangeRate() {
        return Wrapper.OKBuilder.data(enterpriseSearchService.getExchangeRateMap()).build();
    }

    private List<Map> pageList(List list, Integer offset, Integer count) {
        if (list == null) {
            return Lists.newArrayList();
        }
        if (list.size() >= (offset + count)) {
            Integer toIndex = offset + count;
            if (toIndex > list.size()) {
                toIndex = list.size();
            }
            return list.subList(offset, toIndex);
        } else if (list.size() > offset && list.size() < (offset + count)) {
            return list.subList(offset, list.size());
        } else {
            //越界
            return Collections.emptyList();
        }
    }
    
    @RequestMapping(value = "/category", produces = MediaType.APPLICATION_JSON)
    @NoneAuthorization
    public Wrapper category() {
        List<TagCategory> categories = tagCategoryRepo.getAll();
        return Wrapper.OKBuilder.data(categories).build();
    }
    
    @RequestMapping(value = "/tags", produces = MediaType.APPLICATION_JSON)
    @NoneAuthorization
    public Wrapper getTagBycategory(@RequestParam("categoryId") Integer categoryId) {
        List<Tag> tags = tagRepo.findByCondition(null, null, categoryId);
        return Wrapper.OKBuilder.data(tags).build();
    }
    
    @RequestMapping(value = "/tag/search", produces = MediaType.APPLICATION_JSON)
    @NoneAuthorization
    public Wrapper searchTag(@RequestParam("categoryId") Integer categoryId,@RequestParam("keyword") String keyword) {
        List<Tag> tags = tagRepo.searchTags(categoryId, keyword);
        return Wrapper.OKBuilder.data(tags).build();
    }
    
    @RequestMapping(value = "/tag/enable", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON)
    @NoneAuthorization
    public Wrapper enabledTag(@RequestBody Map<String,Object> map) {
    	Integer tagID = Integer.parseInt(map.get("tagId").toString());
    	Integer enabled = Integer.parseInt(map.get("enabled").toString());
        Integer res = tagRepo.enabledTag(tagID, enabled);
        return Wrapper.OKBuilder.data(res).build();
    }
    
    @RequestMapping(value = "/company-tags", produces = MediaType.APPLICATION_JSON)
    @NoneAuthorization
    public Wrapper getTagBycategory(@RequestParam("company") String company) {
        Map<String,Object> tags = customerTagsRepo.findByCondition(company);
        List<CTag> cTags = new ArrayList<CTag>();
        Map<String, List<CTag>> rcTags =new HashMap<String, List<CTag>>();
        ArrayList<Map<String, Object>> res = new ArrayList<Map<String, Object>>();
        if(tags.size()>0){
        	List<Tag> allEnabledTags = tagRepo.getAllEnabledTags();
        	Map<String,Tag> enabledTagsMap = new HashMap<>();
        	allEnabledTags.forEach(item->{
        		enabledTagsMap.put(item.getEnName().trim(), item);
        	});
        	for(Object key:tags.keySet()){  
        		
        		Boolean hasValue = true;
        		String value = "";
        		if(tags.get(key.toString())==null){
        			hasValue = false;
        		}else{
        			value = tags.get(key.toString()).toString(); 
        		}
        		
        	    if(!enabledTagsMap.containsKey(key)|| value.equals("0") || value==null || value.isEmpty()||value.equals("其他") ){
        			hasValue = false;
        		}
        	    Tag tem = enabledTagsMap.get(key);
        	    if(null==tem || null==tem.getValueType()){
        	    	log.warn(key+"标签未配置或无效！");
        	    	continue;
        	    }
        	    if(tem.getValueType()==2 && !"是".equals(value)){
        	    	hasValue = false;
        	    }
        	    
        	    if(tem.getValueType()==2 ){
        	    	value = tem.getCnName().replaceAll("是否", "");
        	    }
        	    
        	    CTag tTag = new CTag();
        	    tTag.setTagName(tem.getCnName());
        	    tTag.setTagValue(value);
        	    tTag.setTagCategory(tem.getCategory());
        	    tTag.setValueType(tem.getValueType());
        	    tTag.setHasValue(hasValue);
        	    
        		if(rcTags.containsKey(tem.getCategory())){
        			List<CTag> tm = rcTags.get(tem.getCategory());
        			tm.add(tTag);
        		}else{
        			List<CTag> tm  = new ArrayList<>();
        			tm.add(tTag);
        			rcTags.put(tem.getCategory(),tm);
        		}
        	}  
        	for(CTag cTag:cTags){
        		if(!enabledTagsMap.containsKey(cTag.getTagName().trim())){
        			continue;
        		}
        		if(rcTags.containsKey(cTag.getTagCategory())){
        			List<CTag> tm = rcTags.get(cTag.getTagCategory());
        			tm.add(cTag);
        		}else{
        			List<CTag> tm  = new ArrayList<>();
        			tm.add(cTag);
        			rcTags.put(cTag.getTagCategory(),tm);
        		}
        	}
        	for (Map.Entry<String, List<CTag>> entry : rcTags.entrySet()) { 
        		 Map<String, Object> tem = new HashMap<>();
        		 tem.put("category",entry.getKey());
        		 tem.put("tags", entry.getValue());
        		 res.add(tem);
        	}
        }
        return Wrapper.OKBuilder.data(res).build();
    }
    
    
    @RequestMapping(value = "/inner-company-link-outer", produces = MediaType.APPLICATION_JSON)
    public Wrapper innerCompanyLinkOuter(@RequestParam("count") Integer count,@RequestParam("offset") Integer offset) {
    	List<CompanyRelate> entities = innerCompanyLinkOuterRepo.findByCondition(offset, count);
    	Long totalCount = Long.parseLong(innerCompanyLinkOuterRepo.count().get("num").toString());
    	DataItem resp = new DataItem(entities, totalCount);
        return Wrapper.OKBuilder.data(resp).build();
    }
    
    @RequestMapping(value = "/tran-corp", produces = MediaType.APPLICATION_JSON)
    public Wrapper tranCorp(@RequestParam("count") Integer count,@RequestParam("offset") Integer offset,@RequestParam("type") Integer type) {
    	List<TRANCORP> entities = tRANCORPRepo.findByCondition(offset, count,type);
    	Long totalCount = Long.parseLong(tRANCORPRepo.count(type).get("num").toString());
    	DataItem resp = new DataItem(entities, totalCount);
        return Wrapper.OKBuilder.data(resp).build();
    }
    
    @RequestMapping(value = "/new-registered-company", produces = MediaType.APPLICATION_JSON)
    public Wrapper newRegisteredCompany(@RequestParam("count") Integer count,@RequestParam("offset") Integer offset,@RequestParam("type") Integer type) {
    	List<NewRegisteredCompany> entities = newRegisteredCompanyRepo.findByCondition(offset, count,type);
    	Long totalCount = Long.parseLong(newRegisteredCompanyRepo.count(type).get("num").toString());
    	DataItem resp = new DataItem(entities, totalCount);
        return Wrapper.OKBuilder.data(resp).build();
    }

    /**
     * 360视图-行业特征
     * @param company
     * @param yearMonth
     * @param index
     * @return
     */
    @RequestMapping(value = "/get-industry_statistics", produces = MediaType.APPLICATION_JSON)
    public Wrapper getIndustryStatistics(@RequestParam("company") String company,
            @RequestParam(value = "yearMonth", required = false) String yearMonth,
            @RequestParam(value = "index", required = false) String index) {
    	Document doc = enterpriseRepo.getBasic(company);
    	String industry =doc.get("industry").toString();

    	List<IndustryStatistics> industryStatistics = industryStatisticsRepo.findByCondition(industry,yearMonth,index);
    	IndustryStatistics industryStatistics2 = new IndustryStatistics();
    	
    	if(industryStatistics.size()>0){
    	   industryStatistics2 = industryStatistics.get(0);
    	   index = industryStatistics2.getIndex();
        	Double value = 0D;
        	Double wind = 0D;
        	boolean hasValue = false;
    		if(doc.get("stock_code")!=null){
    			Document caibao = caiBaoCompaniesAbilityRepo.getBasic(doc.get("stock_code").toString(), industryStatistics2.getYearMonth());
    			if(caibao!=null){
                    Object indexValueObject = caibao.get(index);
    				if(indexValueObject != null && StringUtils.isNotEmpty(indexValueObject.toString())){
    					value = Double.valueOf(indexValueObject.toString().replace(",", ""));
    					hasValue = true;
    				}
    			}
    		}else{
    			Map<String, Object> custInfo = custInfoRepo.findByCondition(company);
    			if(custInfo!=null&&custInfo.containsKey("CUST_ID")){
    				String finIndex = Fields.INDEX_FIELD_TO_COMPNAY.get(index);
    				Map<String, Object> finIndexInfo = finIndexInfoRepo.findByCondition(custInfo.get("CUST_ID").toString(), finIndex, industryStatistics2.getYear());
    				if(finIndexInfo!=null && finIndexInfo.containsKey("cust_id")){
    					String q = Fields.CAIBAO_TYPE_TO_FIN_INDEX_FIELD.get(industryStatistics2.getCaibaoType());
                        Object indexValueObject = finIndexInfo.get(q);
                        if(indexValueObject != null && StringUtils.isNotEmpty(indexValueObject.toString())){
                            value = Double.valueOf(indexValueObject.toString());
                            hasValue = true;
                        }
    				}
    			}
    		}
    		boolean hasWind = false;
    		String reptM = industryStatistics2.getYear()+Fields.CAIBAO_TYPE_TO_MONTH.get(industryStatistics2.getCaibaoType());
    		String averageIndustry = "CSRC"+industry;
    		Map<String, Object> industryAverageInfo = industryAverageInfoRepo.query(reptM, averageIndustry);
    		if(industryAverageInfo!=null){
    			String wf = Fields.INDEX_FIELD_TO_WIND.get(index);
    			if(wf!=null){
                    Object indexValueObject = industryAverageInfo.get(wf);
                    if(indexValueObject != null && StringUtils.isNotEmpty(indexValueObject.toString())){
                        wind = Double.valueOf(indexValueObject.toString());
                        hasWind = true;
                    }

    			}
    		}
    		boolean isRisk = false;
    		if(wind !=0 && (Math.abs((value-wind)/wind) >0.3)){
    			isRisk = true;
    		}
    		industryStatistics2.setWind(wind);
    		industryStatistics2.setValue(value);
        	industryStatistics2.setIsRisk(isRisk);
        	industryStatistics2.setHasValue(hasValue);
        	industryStatistics2.setHasWind(hasWind);
    	}
    	
        return Wrapper.OKBuilder.data(industryStatistics2).build();
    }

    @ApiOperation(value = "根据族谱名称从mysql和monogodb获取公司列表")
    @RequestMapping(value = "/group_company", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    public Wrapper getGroupCompany(@RequestParam(value = "circle") @ApiParam(value = "族谱名称", required = true)String groupName,
                                   @RequestParam(value = "type") @ApiParam(value = "族谱类型", required = true)String groupType) {

        GroupMembersSearchQo groupMembersSearchQo = new GroupMembersSearchQo();
        groupMembersSearchQo.setGroupName(groupName);
        groupMembersSearchQo.setEntityType(EntityType.COMPANY);
        groupMembersSearchQo.setGroupType(groupType);
        GroupMembersVo groupMembersVo = groupService.findGroupMembers(groupMembersSearchQo);

        GroupCompanyVo result = new GroupCompanyVo();
        result.setCompanies(groupMembersVo.getMembers());
        result.setNum(groupMembersVo.getMembers().size());

        return Wrapper.ok(result);
    }


    @ApiOperation(value = "根据集团名获取该集团下的自然人列表")
    @RequestMapping(value = "/group_person", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    public Wrapper queryGroupPerson(@RequestParam @ApiParam(value = "公司名称",name = "groupName",example = "安徽电气集团股份有限公司",required = true)String groupName){
        if (Strings.isNullOrEmpty(groupName)) {
            return SearchException.MISS_COMPANY.get();
        }
        Map<String,Object> map = new HashMap<>();
        List<Map> groupPerson = graphClustersService.queryPersonMembers(groupName);
        map.put("data",groupPerson);
        map.put("total",groupPerson.size());
        return Wrapper.ok(map);
    }
    
    @RequestMapping(value = "/get-caibao-type", produces = MediaType.APPLICATION_JSON)
    public Wrapper getCaibaoType(@RequestParam("company") String company) {
    	Document doc = enterpriseRepo.getBasic(company);
    	String industry =doc.get("industry").toString();
    	List<Map<String, Object>> caibaoType = industryStatisticsRepo.findCaibaoType(industry);
    	Map<String, List<String>> result = new HashMap<String, List<String>>();
    	caibaoType.forEach(item->{
    		String tyear = item.get("year_month").toString().substring(0, 4);
    		String tcaibaoType = item.get("year_month").toString().substring(4);
    		if(result.containsKey(tyear)){
    			result.get(tyear).add(tcaibaoType);
    		}else{
    			List<String> ct = new ArrayList<>();
    			ct.add(tcaibaoType);
    			result.put(tyear, ct);
    		}
    	});
        return Wrapper.OKBuilder.data(result).build();
    }
    
    
    @RequestMapping(value = "/beneficiary", produces = MediaType.APPLICATION_JSON)
    public Wrapper beneficiary(@RequestParam("count") Integer count,@RequestParam("offset") Integer offset) {
    	List<Beneficiary> entities = beneficiaryRepo.findByCondition(offset, count);
    	entities.forEach(item->{
			if(Double.valueOf(item.getActualControlRatio())==0){
				item.setActualControlRatio("--");
			}
    	});
    	Long totalCount = Long.parseLong(beneficiaryRepo.count().get("num").toString());
    	DataItem resp = new DataItem(entities, totalCount);
        return Wrapper.OKBuilder.data(resp).build();
    }
    
	@RequestMapping(value = "/beneficiary/download")
	public AbstractXlsView beneficiaryDownload( HttpServletRequest request,
            HttpServletResponse response) throws UnsupportedEncodingException {
		List<Beneficiary> entities = beneficiaryRepo
				.findByCondition(null, null);
		String excelName = "对公客户受益人.xls";
		 response.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode(excelName,"utf-8"));
	        response.setContentType("application/ms-excel; charset=UTF-8");
	        response.setCharacterEncoding("UTF-8");
		AbstractXlsView xls = new AbstractXlsView() {
			@Override
			protected void buildExcelDocument(Map<String, Object> map,
					org.apache.poi.ss.usermodel.Workbook workbook,
					HttpServletRequest httpServletRequest,
					HttpServletResponse httpServletResponse) throws Exception {
			        
					Sheet sheet = workbook.createSheet("受益人列表");
					Row header = sheet.createRow(0);
			        header.createCell(0).setCellValue("客户号");
			        header.createCell(1).setCellValue("企业名称");
			        header.createCell(2).setCellValue("证件号码");
			        header.createCell(3).setCellValue("证件类型");
			        header.createCell(4).setCellValue("受益人");
			        header.createCell(5).setCellValue("受益人类型");
			        header.createCell(6).setCellValue("股权比例");
			        header.createCell(7).setCellValue("是否为实际控制人");
			        
					int size = entities.size();
					for(int i =0 ;i<size;i++){
						Beneficiary item = entities.get(i);
						Row row = sheet.createRow(i+1);
						row.createCell(0).setCellValue(item.getCustId());
						row.createCell(1).setCellValue(item.getCompany());
						row.createCell(2).setCellValue(item.getCertNo());
						row.createCell(3).setCellValue(item.getCertType());
						row.createCell(4).setCellValue(item.getPerson());
						row.createCell(5).setCellValue(item.getBenefitType());
						if(Double.valueOf(item.getActualControlRatio())==0){
							row.createCell(6).setCellValue("--");
						}else{
							row.createCell(6).setCellValue(item.getActualControlRatio());
						}
						
						row.createCell(7).setCellValue(item.getIsController());
					
					}
				}
		};
		return xls;
	}
    
}
