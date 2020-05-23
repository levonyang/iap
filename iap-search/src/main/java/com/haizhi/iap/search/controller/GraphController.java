package com.haizhi.iap.search.controller;

import com.google.common.base.Strings;
import com.haizhi.iap.common.Wrapper;
import com.haizhi.iap.common.auth.NoneAuthorization;
import com.haizhi.iap.common.utils.SecretUtil;
import com.haizhi.iap.search.controller.model.AnnualReportQuery;
import com.haizhi.iap.search.controller.model.DataItem;
import com.haizhi.iap.search.controller.model.EdgesOption;
import com.haizhi.iap.search.controller.model.Graph;
import com.haizhi.iap.search.controller.model.GraphVo;
import com.haizhi.iap.search.controller.model.GraphListReq;
import com.haizhi.iap.search.controller.model.GraphOptions;
import com.haizhi.iap.search.controller.model.GraphReq;
import com.haizhi.iap.search.controller.model.PageResult;
import com.haizhi.iap.search.controller.model.ReqGuaranteeOrTransfer;
import com.haizhi.iap.search.controller.model.Tree;
import com.haizhi.iap.search.enums.ESEnterpriseSearchType;
import com.haizhi.iap.search.exception.SearchException;
import com.haizhi.iap.search.model.CompanyGroup;
import com.haizhi.iap.search.model.GraphCompany;
import com.haizhi.iap.search.model.GraphEdge;
import com.haizhi.iap.search.repo.ESEnterpriseSearchRepo;
import com.haizhi.iap.search.repo.EnterpriseRepo;
import com.haizhi.iap.search.service.EnterpriseSearchService;
import com.haizhi.iap.search.service.GraphClustersService;
import com.haizhi.iap.search.service.GraphExplainService;
import com.haizhi.iap.search.service.GraphGroupService;
import com.haizhi.iap.search.service.GraphService;
import com.haizhi.iap.search.service.GroupService;
import com.haizhi.iap.search.utils.BankUtil;
import com.haizhi.iap.search.utils.CompanyGroupDataTransformer;
import com.haizhi.iap.search.utils.EventDataTransformer;
import com.haizhi.iap.search.utils.InvestGroupDataTransformer;
import com.haizhi.iap.search.utils.MoneyFlowDataTransformer;
import com.haizhi.iap.search.utils.OfficerDataTransformer;
import com.haizhi.iap.search.utils.StockRightDataTransformer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by chenbo on 17/2/22.
 */
@Api(tags="【搜索-公司图谱模块】查看图谱信息")
@Slf4j
@RestController
@RequestMapping("/graph")
public class GraphController {

    @Setter
    @Autowired
    GraphService graphService;

    @Autowired
    private GroupService groupService;

    @Setter
    @Autowired
    GraphExplainService graphExplainService;

    @Setter
    @Autowired
    EnterpriseRepo enterpriseRepo;

    @Setter
    @Autowired
    EnterpriseSearchService enterpriseSearchService;

    @Setter
    @Autowired
    ESEnterpriseSearchRepo esEnterpriseSearchRepo;

    @Setter
    @Autowired
    GraphReq graphOriginalReq;

    @Setter
    @Autowired
    GraphClustersService graphClustersService;

    @Setter
    @Autowired
    GraphGroupService graphGroupService;


    private static List<String> detailList =
            Arrays.asList("judgement_wenshu", "judge_process", "bid_detail", "court_ktgg", "bulletin");

    /**
    * @description 通过公司名获取公司的关系概览
    * @param name  公司名称
    * @return com.haizhi.iap.common.Wrapper
    * @author yuding
    * @date 2018/8/20
    */
    @ApiOperation(value = "关系图谱-关系概览", notes = "通过公司名获取公司的关系概览")
    @RequestMapping(value = "/overviewRelation", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    public Wrapper overviewRelation(@RequestParam @ApiParam(name = "name",value ="公司名称",example = "安徽电气集团股份有限公司")String name){
        if (Strings.isNullOrEmpty(name)) {
            return SearchException.MISS_COMPANY.get();
        }
        Tree tree  = graphService.queryOverviewRelation(name);
        return Wrapper.ok(tree);
    }

    /**
     * 公司简介
     *
     * @param company
     * @return
     */
    @RequestMapping(value = "/brief", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    @NoneAuthorization
    public Wrapper brief(@RequestParam("company") String company) {
        if (Strings.isNullOrEmpty(company)) {
            return SearchException.MISS_COMPANY.get();
        }
        Map<String, Object> com = enterpriseRepo.getBasic(company);
        if (com == null) {
            return SearchException.NO_SUCH_COMPANY.get();
        }
        return Wrapper.ok(com);
    }


    /**
     * 公司简介（批量接口）
     *
     * @param companys
     * @return
     */
    @RequestMapping(value = "/briefBatch", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON)
    @NoneAuthorization
    public Wrapper briefBatch(@RequestBody List<String> companys) {
        if (CollectionUtils.isEmpty(companys)) {
            return SearchException.MISS_COMPANY.get();
        }
        List<Map<String, Object>> result = enterpriseSearchService.briefBatch(companys);
        return Wrapper.OKBuilder.data(result).build();
    }

    /**
     * 企业图谱
     *
     * @param req
     * @return
     */
    @RequestMapping(value = "/company", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper companyGraph(@RequestParam(name = "read_cache", required = false, defaultValue = "true") Boolean readCache,
                                @RequestBody GraphReq req) {
        if (Strings.isNullOrEmpty(req.getCompany())) {
            return SearchException.MISS_COMPANY.get();
        }
        if (req.getOptions() == null) {
            return SearchException.MISS_OPTIONS.get();
        }
        GraphOptions options = req.getOptions();
        if (options.getEdges() == null) {
            return SearchException.MISS_EDGES.get();
        }
        if (options.getMinWeight() == null) {
            options.setMinWeight(0);
        }

        Map<String, Object> graphCompany = graphService.brief(req.getCompany());
        if (graphCompany == null) {
            return SearchException.NO_SUCH_COMPANY.get();
        } else if (graphCompany.get("_id") == null) {
            return SearchException.WRONG_ARANGO_DATA.get();
        }

        Graph graph = graphService.buildGraph(graphCompany.get("_id").toString(), req, readCache);
        return Wrapper.OKBuilder.data(graph).build();
    }

    /**
     * 给微办公爬的接口
     *
     * @return
     */
    @RequestMapping(value = "/invest", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper invest(@RequestParam("company") String companyName) {
        if (Strings.isNullOrEmpty(companyName)) {
            return SearchException.MISS_COMPANY.get();
        }
        Map<String, Object> company = enterpriseRepo.getBasic(companyName);
        Map<String, Object> graphCompany = graphService.brief(companyName);
        String stockCode = null;
        if (company.get("stock_code") != null) {
            stockCode = company.get("stock_code").toString();
        }

        if (graphCompany == null) {
            return SearchException.NO_SUCH_COMPANY.get();
        }

        GraphReq req = (GraphReq) graphOriginalReq.clone();
        req.setCompany(companyName);
        for (EdgesOption option : req.getOptions().getEdges()) {
            if (option.getCategory().equals("invest")) {
                option.setVisible(true);
            } else if (option.getCategory().equals("shareholder")) {
                if (!Strings.isNullOrEmpty(stockCode)) {
                    option.setCategory("tradable_share");
                    option.setVisible(true);
                }
            } else {
                option.setVisible(false);
            }
        }

        return Wrapper.ok(graphService.invest((String) graphCompany.get("_id"), req));
    }

    /**
     * 展开/隐藏实体详情
     *
     * @param id
     * @param req
     * @return
     */
    @RequestMapping(value = "/by_id", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper getGraphById(@RequestParam(value = "id", required = false) String id,
                                @RequestBody GraphReq req) {
        //后面做了变更,可传可不传
//        if (Strings.isNullOrEmpty(id)) {/v2
//            return SearchException.MISS_ID.get();
//        }
        if (req.getOptions() == null) {
            return SearchException.MISS_OPTIONS.get();
        }
        GraphOptions options = req.getOptions();
        if (options.getEdges() == null) {
            return SearchException.MISS_EDGES.get();
        }
        if (options.getMinWeight() == null) {
            options.setMinWeight(0);
        }
        if (!Strings.isNullOrEmpty(req.getCompany())) {
            Map<String, String> stockTypeMap = enterpriseRepo.getSectorMap(req.getCompany());
            options.getEdges().stream().filter(option -> option.getCategory().equals("shareholder")).forEach(option -> {
                if (option.getVisible() != null) {
                    if (option.getVisible() && stockTypeMap != null && stockTypeMap.values().size() > 0) {
                        option.setCategory("tradable_share");
                    }
                }
            });
        }
        return Wrapper.OKBuilder.data(graphService.buildGraph(id, req, false)).build();
    }

    /**
     * 可能认识的人和关联公司
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/familiars", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper getFamiliars(@RequestParam("id") String id) {
        if (Strings.isNullOrEmpty(id)) {
            return SearchException.MISS_ID.get();
        }

        return Wrapper.OKBuilder.data(graphService.getFamiliars(id)).build();
    }

    /**
     * 可能认识的人关系图
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/familiar", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper getFamiliar(@RequestParam("id") String id,
                               @RequestParam(value = "target", required = false) String target) {
        if (Strings.isNullOrEmpty(id)) {
            return SearchException.MISS_ID.get();
        }

        return Wrapper.OKBuilder.data(graphService.getFamiliar(id, target)).build();
    }

    /**
     * 两个人疑似可融合人的关联
     *
     * @param personA
     * @param personB
     * @return
     */
    @RequestMapping(value = "/person_merge_suggested", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper mergeSuggested(@RequestParam("person1") String personA,
                                  @RequestParam("person2") String personB) {
        if (Strings.isNullOrEmpty(personA)) {
            return SearchException.MISS_PERSONA.get();
        }
        if (Strings.isNullOrEmpty(personA)) {
            return SearchException.MISS_PERSONB.get();
        }
        return Wrapper.OKBuilder.data(graphService.getPersonMergeSuggested(personA, personB)).build();
    }

    /**
     * 查找疑似可融合人的列表
     *
     * @param personId
     * @return
     */
    @RequestMapping(value = "/person_merge_suggested_list", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper mergeSuggestedList(@RequestParam("id") String personId) {
        if (Strings.isNullOrEmpty(personId)) {
            return SearchException.MISS_ID.get();
        }

        return Wrapper.OKBuilder.data(graphService.getPersonMergeSuggestedList(personId)).build();
    }

    /**
     * 关联查询(关系路径，主体代码在foxx中)
     *
     * @param companyA
     * @param companyB
     * @param req
     * @return
     */
    @ApiOperation(value = "查找两家企业之间的关系")
    @RequestMapping(value = "/relation", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper relation(@ApiParam(value = "企业A", required = true)@RequestParam("company1") String companyA,
                            @ApiParam(value = "企业B", required = true)@RequestParam(value = "company2", required = false) String companyB,
                            @RequestBody GraphReq req) {
        if (Strings.isNullOrEmpty(companyA)) {
            return SearchException.MISS_COMPANY1.get();
        } else if (req.getOptions() == null) {
            return SearchException.MISS_OPTIONS.get();
        }
        GraphOptions options = req.getOptions();
        if (options.getEdges() == null) {
            return SearchException.MISS_EDGES.get();
        }

        if (companyA.startsWith("Company/")) {
            return Wrapper.ok(graphService.getRelationById(companyA, companyB, req));
        } else {
            return Wrapper.ok(graphService.getRelationByName(companyA, companyB, req));
        }
    }

    /**
     * 查询担保环（检验路径）
     *
     * @param companyA
     * @param companyB
     * @return
     */
    @RequestMapping(value = "/guarantee_circle", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper checkGuaranteeCircle(@RequestParam("company1") String companyA,
                                        @RequestParam(value = "company2", required = false) String companyB) {
        if (Strings.isNullOrEmpty(companyA)) {
            return SearchException.MISS_COMPANY1.get();
        }
        return Wrapper.OKBuilder.data(graphService.checkGuaranteeCircle(companyA, companyB)).build();
    }

    @RequestMapping(value = "/group_by_company", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper groupByCompany(@RequestParam("company") String companyName) {
        if (Strings.isNullOrEmpty(companyName)) {
            return SearchException.MISS_COMPANY.get();
        }

        return Wrapper.OKBuilder.data(graphService.groupByCompany(companyName)).build();
    }

    @RequestMapping(value = "/group_by_id", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper groupById(@RequestParam("group_id") String groupId) {
        if (Strings.isNullOrEmpty(groupId)) {
            return SearchException.MISS_GROUP_ID.get();
        }

        return Wrapper.OKBuilder.data(graphService.groupById(groupId)).build();
    }

    /**
     * 社区发现
     *
     * @param entityId
     * @return
     */
    @RequestMapping(value = "/group_by_entity_id", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper groupByEntityId(@RequestParam("entity_id") String entityId) {
        if (Strings.isNullOrEmpty(entityId)) {
            return SearchException.MISS_ENTITY_ID.get();
        }

        return Wrapper.OKBuilder.data(graphService.groupByEntityId(entityId)).build();
    }

    @RequestMapping(value = "/case_detail", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper getCaseDetail(@RequestParam("collection") String collection,
                                 @RequestParam("record_id") String recordId) {
        if (Strings.isNullOrEmpty(collection)) {
            return SearchException.MISS_COLLECTION.get();
        }
        if (Strings.isNullOrEmpty(recordId)) {
            return SearchException.MISS_RECORD_ID.get();
        }
        if (!detailList.contains(collection)) {
            return SearchException.WRONG_COLLECTION.get();
        }
        Document doc = enterpriseRepo.findByCollAndId(collection, recordId);
        if (doc == null) {
            return SearchException.NO_DATA.get();
        } else {
            return Wrapper.OKBuilder.data(doc).build();
        }
    }

    @RequestMapping(value = "/search_suggest", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper searchSuggest(@RequestParam("key_word") String keyWord,
                                 @RequestParam("type") String type,
                                 @RequestParam("count") Integer count) throws IOException {
        if (Strings.isNullOrEmpty(keyWord)) {
            return SearchException.MISS_KEY_WORD.get();
        }
        if (Strings.isNullOrEmpty(type)) {
            type = ESEnterpriseSearchType.NAME.getName();
        }
        Integer searchSize = 10;
        if (count != null) {
            searchSize = count > 20 ? 20 : count;
        }
        List<GraphCompany> result = esEnterpriseSearchRepo.graphSuggest(keyWord, type, 200, searchSize);
        return Wrapper.OKBuilder.data(result).build();
    }

    /**
     * 获取对外投资/任职的关系图
     *
     * @param key Person / Company -> _id
     * @return
     */
    @RequestMapping(value = "/invest_officer_around", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    public Wrapper investOfficerAround(@RequestParam("key") String key,
                                       @RequestParam(value = "depth", required = false, defaultValue = "1") int depth,
                                       @RequestParam(value = "personMergeDepth", required = false, defaultValue = "1") int personMergeDepth,
                                       @RequestParam(value = "limit", required = false, defaultValue = "100") int limit) {
        Map<String, Object> data = graphService.investOfficerAround(key, depth, personMergeDepth, limit);
        return Wrapper.OKBuilder.data(data).build();
    }

    /**
     * 获取簇列表
     */
//    @RequestMapping(value = "/clusters", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
//    public Wrapper getClusters(@RequestParam(value = "domain_name") String domainName,
//                               @RequestParam(value = "type") String type,
//                               @RequestParam(value = "group_type", defaultValue = "_all") String groupType,
//                               @RequestParam(value = "offset", required = false) Integer offset,
//                               @RequestParam(value = "count", required = false) Integer count) {
//        if (Strings.isNullOrEmpty(domainName) || Strings.isNullOrEmpty(type)) {
//            return SearchException.MISS_DOMAIN.get();
//        }
//        if (offset == null) {
//            offset = 0;
//        }
//        if (count == null) {
//            count = 5;
//        }
//        if (Strings.isNullOrEmpty(groupType)) {
//            return SearchException.MISS_GROUP_TYPE.get();
//        }
//        DataItem item = graphClustersService.getIgnorePaths(domainName, type, groupType, offset, count);
//        return Wrapper.OKBuilder.data(item).build();
//    }


    /**
    * @description 获取指定族谱的边
    * @param type 族谱类型
     *（如profile_enterprise_info 关联集团
     * market_updown_info  上下游
     * risk_propagation  风险传导
     * risk_guarantee_info 关联担保
     * risk_black_info  黑名单）
    * @param subType 族谱子类型（如circle）
    * @param offset 开始行数
    * @param count 返回数量
    * @return com.haizhi.iap.common.Wrapper
    * @author LewisLouis
    * @date 2018/8/17
    */
    @RequestMapping(value = "/groups", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    public Wrapper findGroupsWithOutPaths(@RequestParam(value = "type") String type,
                             @RequestParam(value = "sub_type", defaultValue = "_all") String subType,
                             @RequestParam(value = "offset", required = false) Integer offset,
                             @RequestParam(value = "count", required = false) Integer count) {
        if (Strings.isNullOrEmpty(type)) {
            return SearchException.MISS_TYPE.get();
        }

        if ((Strings.isNullOrEmpty(subType))) {
            return SearchException.MISS_SUB_TYPE.get();
        }

        if (null == offset){
            offset = 0;
        }

        if (null == count){
            count = 5;
        }

        DataItem item = graphGroupService.findGroupsByTypeWithOutPaths(type, subType, offset, count);
        return Wrapper.ok(item);
    }

    /**
     * 根据簇id获取簇子图
     */
//    @RequestMapping(value = "/cluster", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
//    @NoneAuthorization
//    public Wrapper getClusterByCid(@RequestParam(value = "cid") String cid,
//                                   @RequestParam(value = "result_type", defaultValue = "graph") String resultType,
//                                   @RequestParam(value = "domain_name") String domainName,
//                                   @RequestParam(value = "type") String type) {
//        if (Strings.isNullOrEmpty(domainName) || Strings.isNullOrEmpty(type)) {
//            return SearchException.MISS_DOMAIN.get();
//        }
//
//        if (resultType.equals("graph")) {
//            Graph result = graphClustersService.getCluster(domainName, type, cid);
//
//            return Wrapper.OKBuilder.data(result).build();
//        } else if (resultType.equals("path")) {
//            ClusterGroup group = graphClustersService.findByCid(domainName, type, cid);
//            return Wrapper.OKBuilder.data(group).build();
//        } else {
//            return SearchException.WRONG_RESULT_TYPE.get();
//        }
//    }

    /**
    * @description 根据族谱名字获取族谱簇子图
    * @param groupName 族谱名称
    * @param resultType 返回格式类型
    * @param type 族谱类型
    * @return com.haizhi.iap.common.Wrapper
    * @author LewisLouis
    * @date 2018/8/20
    */
    @ApiOperation(value = "根据族谱名字和类型获取对应的族谱图")
    @RequestMapping(value = "/group_info", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    public Wrapper findOneGroup(@ApiParam(value = "族谱名称", required = true)
                                @RequestParam(value = "group_name") String groupName,
                                @ApiParam(value = "返回的结果类型：graph:返回图数据，path:返回导入到mysql中的数据", required = true)
                                @RequestParam(value = "result_type", defaultValue = "graph") String resultType,
                                @ApiParam(value = "族谱类型, profile_enterprise_info 关联集团, market_updown_info 上下游," +
                                        " risk_propagation  风险传导,risk_guarantee_info 关联担保 risk_black_info  黑名单", required = true)
                                 @RequestParam(value = "type") String type) {
        if (Strings.isNullOrEmpty(type)) {
            return SearchException.MISS_TYPE.get();
        }

        if (Strings.isNullOrEmpty(groupName)) {
            return SearchException.MISS_GROUP_NAME.get();
        }

        if (resultType.equals("graph")){
            Graph resGraph = graphGroupService.findOneGroupGraph(groupName,type);
            return Wrapper.ok(resGraph);
        } else if (resultType.equals("path")){
            CompanyGroup group = graphGroupService.findOneGroup(groupName,type);
            return Wrapper.ok(group);
        } else{
            return SearchException.WRONG_RESULT_TYPE.get();
        }
    }

    /**
    * @description 根据实体信息获取所在族谱的名称
    * @param entityId
    * @param type
    * @return com.haizhi.iap.common.Wrapper
    * @author LewisLouis
    * @date 2018/8/20
    */
    @RequestMapping(value = "/group_name", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    public Wrapper findGroupByEntityId(@RequestParam(value = "entity_id") String entityId,
                                      @RequestParam(value = "type") String type) {
        if (Strings.isNullOrEmpty(type)) {
            return SearchException.MISS_TYPE.get();
        }
        if (Strings.isNullOrEmpty(entityId)) {
            return SearchException.MISS_ENTITY.get();
        }
        String groupName = graphGroupService.findGroupNameByEntity(entityId, type);
        return Wrapper.ok(groupName);
    }

    /**
    * @description 统计指定族谱类型的边类型数量
    * @param type 族谱类型
    * @return com.haizhi.iap.common.Wrapper
    * @author LewisLouis
    * @date 2018/8/20
    */
    @RequestMapping(value = "/group_path_types", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    public Wrapper groupPath(@RequestParam(value = "type") String type) {
        if (Strings.isNullOrEmpty(type)) {
            return SearchException.MISS_TYPE.get();
        }
        Map<String, Long> data = graphGroupService.findSubTypes(type);
        return Wrapper.ok(data);
    }
    

   /**
    * @description 根据族谱类型获取边列表
    * @param type 族谱类型
    * @param subType 族谱边类型
    * @param offset
    * @param count
    * @return com.haizhi.iap.common.Wrapper
    * @author LewisLouis
    * @date 2018/8/20
    */
    @RequestMapping(value = "/group_path", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    public Wrapper findGroupPaths(@RequestParam(value = "type") String type,
                             @RequestParam(value = "sub_type") String subType,
                             @RequestParam(value = "offset", defaultValue = "0") Integer offset,
                             @RequestParam(value = "count", defaultValue = "5") Integer count) {
        if (Strings.isNullOrEmpty(type)) {
            return SearchException.MISS_TYPE.get();
        }
        if (Strings.isNullOrEmpty(subType)) {
            return SearchException.MISS_SUB_TYPE.get();
        }
        DataItem data = graphGroupService.findGroupPaths(type, subType, offset, count);
        return Wrapper.OKBuilder.data(data).build();
    }


    /**
     * 与实体相关的路径
     */
//    @NoneAuthorization
//    @RequestMapping(value = "/entity_paths", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
//    public Wrapper getPathsByName(@RequestParam(value = "entity_name", required = false) String entityName,
//                                  @RequestParam(value = "entity_id", required = false) String entityId,
//                                  @RequestParam(value = "domain_name") String domainName,
//                                  @RequestParam(value = "type") String type) {
//        if (Strings.isNullOrEmpty(domainName) || Strings.isNullOrEmpty(type)) {
//            return SearchException.MISS_DOMAIN.get();
//        }
//        if (Strings.isNullOrEmpty(entityName) && Strings.isNullOrEmpty(entityId)) {
//            return SearchException.MISS_ENTITY.get();
//        }
//        String finalEntityId;
//        if (!Strings.isNullOrEmpty(entityId)) {
//            finalEntityId = entityId;
//        } else {
//            finalEntityId = "Company/" + SecretUtil.md5(entityName);
//        }
//        List<ClusterPath> data = graphClustersService.findPaths(domainName, type, finalEntityId);
//        return Wrapper.OKBuilder.data(data).build();
//    }

    /**
    * @description 获取实体相关的路径(仅支持公司实体)
    * @param entityName 实体名称
    * @param entityId 实体id
    * @param type 族谱类型
    * @return com.haizhi.iap.common.Wrapper
    * @author LewisLouis
    * @date 2018/8/20
    */
    @RequestMapping(value = "/group_by_entity", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    public Wrapper getGroupByEntity(@RequestParam(value = "entity_name", required = false) String entityName,
                                  @RequestParam(value = "entity_id", required = false) String entityId,
                                  @RequestParam(value = "type") String type) {
        if (Strings.isNullOrEmpty(type)) {
            return SearchException.MISS_GROUP_TYPE.get();
        }
        if (Strings.isNullOrEmpty(entityName) && Strings.isNullOrEmpty(entityId)) {
            return SearchException.MISS_ENTITY.get();
        }
        String finalEntityId;
        if (!Strings.isNullOrEmpty(entityId)) {
            finalEntityId = entityId;
        } else {
            finalEntityId = "Company/" + SecretUtil.md5(entityName);
        }
        List<CompanyGroup> data = graphGroupService.findEntityPaths(type, finalEntityId);
        /**
         * 最终结果: add by linyong 2020-04-12
         * [
         *      {
         *         "groupName":"", //实体名称，可能是个人或企业公司名称
         *         "type":"risk_guarantee_info", //族谱信息类型,eg:risk_guarantee_info、risk_black_info...
         *         "subType":"", //族谱子类型，eg:circle
         *         "vertexes":[
         *              {
         *                  "_id":"",
         *                  "name":"",
         *                  ...具体见person和company的结构
         *              }
         *         ],
         *         "entityCount":12, //实体数量，包括个人和企业
         *         "belongInner":true, //实体是否是行内客户
         *         "innerEntityCount":12, //行内客户的数量
         *         "createTime":"xxxx", //数据插入时间
         *         "updateTime":"xxxx", //数据更新时间
         *      }
         * ]
         */
        return Wrapper.ok(data);
    }

    /**
     * 族谱类型统计
     */
//    @RequestMapping(value = "/group_types", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
//    public Wrapper getPathsByName(@RequestParam(value = "domain_name") String domainName,
//                                  @RequestParam(value = "type") String type) {
//        if (Strings.isNullOrEmpty(domainName) || Strings.isNullOrEmpty(type)) {
//            return SearchException.MISS_DOMAIN.get();
//        }
//
//        List<TypeCount> data = graphClustersService.getGroupTypes(domainName, type);
//        return Wrapper.OKBuilder.data(data).build();
//    }

    /**
     * 根据实体id查询其所在族谱
     */
//    @RequestMapping(value = "/find_group", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
//    public Wrapper findGroup(@RequestParam(value = "domain_name") String domainName,
//                             @RequestParam(value = "type") String type,
//                             @RequestParam(value = "entity_id", required = false) String entityId,
//                             @RequestParam(value = "entity_name", required = false) String entityName) {
//        if (Strings.isNullOrEmpty(domainName) || Strings.isNullOrEmpty(type)) {
//            return SearchException.MISS_DOMAIN.get();
//        }
//
//        if (Strings.isNullOrEmpty(entityName) && Strings.isNullOrEmpty(entityId)) {
//            return SearchException.MISS_ENTITY.get();
//        }
//        String finalEntityId;
//        if (!Strings.isNullOrEmpty(entityId)) {
//            finalEntityId = entityId;
//        } else {
//            finalEntityId = "Company/" + SecretUtil.md5(entityName);
//        }
//
//        ClusterGroup group = graphClustersService.findGroup(domainName, type, finalEntityId);
//        return Wrapper.OKBuilder.data(group).build();
//    }

    /**
     * 担保 或 转账 详情 列表 此接口通用
     */
    @RequestMapping(value = "/getDetailList", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper getDetailList(@RequestBody ReqGuaranteeOrTransfer req) {
        PageResult result = this.graphService.getArangoListByPage(req);
        if ("Personal_account".equals(req.getType())) {
            for (Map<String, Object> item : result.getList()) {
                item.put("bankInfo", BankUtil.getNameOfBank(item.get("no").toString()));
            }
        }
        return Wrapper.OKBuilder.data(result).build();
    }

    /**
     * 获取 单表 单点 所有路径
     */
    @RequestMapping(value = "/getPathBySingleCollection", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper getPathBySingleCollection(@RequestParam(value = "collection") String collection,
                                             @RequestParam(value = "depth") int depth, @RequestParam(value = "id") String id) {
        Map<String, List> result = this.graphService.getPathBySingleCollection(collection, depth, id);
        return Wrapper.OKBuilder.data(result).build();
    }

    /**
     * "解释"实际控制人
     */
    @RequestMapping(value = "/explain/actual_control", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper explainActualControl(@RequestParam(value = "rule") String rule,
                                        @RequestParam(value = "from_list") String froms,
                                        @RequestParam(value = "depth_list") String depths,
                                        @RequestParam(value = "to") String to) {
        if (Strings.isNullOrEmpty(froms)) {
            return SearchException.MISS_FROM_LIST.get();
        }
        if (Strings.isNullOrEmpty(depths)) {
            return SearchException.MISS_DEPTH_LIST.get();
        }
        List<String> fromList = Arrays.asList(froms.split(","));
        List<String> depthList = Arrays.asList(depths.split(","));
        Graph graph = graphExplainService.explainActualControl(rule.trim(), fromList, depthList, to);

        return Wrapper.OKBuilder.data(graph).build();
    }

    /**
     * "解释"一致行动人
     */
    @RequestMapping(value = "/explain/concert", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper explainConcert(@RequestParam(value = "from") String from,
                                  @RequestParam(value = "to") String to,
                                  @RequestParam(value = "target") String target,
                                  @RequestParam(value = "rule") String rule) {
        Graph graph = graphExplainService.explainConcert(from, to, target, rule);
        return Wrapper.OKBuilder.data(graph).build();
    }

    /**
     * 导入族谱
     */
//    @RequestMapping(value = "/import/clusters", method = RequestMethod.POST,
//            consumes = MediaType.MULTIPART_FORM_DATA, produces = MediaType.APPLICATION_JSON)
//    public Wrapper importClusters(
//            @RequestParam(value = "file", required = false) MultipartFile multipartFile,
//            @RequestParam(value = "domain_id", required = false) Long domainId,
//            @RequestParam(value = "domain_name", required = false) String domain_name,
//            @RequestParam(value = "type", required = false) String type) {
//        if (domainId == null && domain_name == null && type == null) {
//            return SearchException.MISS_DOMAIN.get();
//        }
//        try {
//            InputStream inputStream = multipartFile.getInputStream();
//            if (inputStream != null) {
//                if (domainId != null && domainId != 0) {
//                    graphClustersService.importClustersFromInput(inputStream, domainId);
//                } else if (domain_name != null && type != null) {
//                    graphClustersService.importClustersFromInput(inputStream, domain_name, type);
//                } else {
//                    return SearchException.WRONG_DOMAIN_COMB.get();
//                }
//            }
//        } catch (IOException e) {
//            return Wrapper.ERRORBuilder.data(e.getMessage()).build();
//        }
//        return Wrapper.OK;
//    }

    /**
    * @description 导入族谱(导入到company_group和group_detail中)
    * @param multipartFile
    * @param type
    * @return Wrapper
    * @author yuding
    * @date
    */
    @ApiOperation(value = "导入族谱文件",notes = "将数据文件导入到company_group和group_detail两张表中")
    @RequestMapping(value = "/import/groups", method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA, produces = MediaType.APPLICATION_JSON)
    public Wrapper importClusters(
            @RequestParam(value = "file") @ApiParam(name = "file",value = "文件名称", example = "enterprise_group.dms",required = true) MultipartFile multipartFile,
            @RequestParam(value = "type") @ApiParam(name = "type",value = "文件类型", example = "profile_enterprise_info",required = true) String type) {
        if (type == null) {
            return SearchException.MISS_TYPE.get();
        }

        try {
            InputStream inputStream = multipartFile.getInputStream();
            if (inputStream == null) {
                return SearchException.FILE_NOT_FOUND.get();
            }
            graphClustersService.importGroupsFromInput(inputStream, type);

        } catch (IOException e){
            return Wrapper.ERRORBuilder.data(e.getMessage()).build();
        }
        return Wrapper.OK;
    }

    /**
    * @description 存量客户洞察面板统计
    * @param
    * @return com.haizhi.iap.common.Wrapper
    * @author yuding
    * @date 2018/8/23
    */
    @ApiOperation(value = "洞察面板",notes = "存量客户洞察面板统计,统计不同族谱类型包含的集团数量，不需要参数")
    @RequestMapping(value = "/count_by_type", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    public Wrapper countType() {
        try {
            List<Map<String, Object>> map = graphClustersService.countByType();
            return Wrapper.ok(map);
        } catch (Exception e){
            log.error("", e);
            return Wrapper.ok(e.getMessage());
        }
    }

    /**
     * @description 根据族谱类型和企业名获取族谱列表
     * @param @RequestBody GraphListReq
     * @return Wrapper
     * @author yuding
     * @date 2018/08/18
     */
    @ApiOperation(value = "族谱列表",notes = "根据族谱类型和企业名获取包含该企业的族谱列表")
    @RequestMapping(value = "/group_list", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper getGrouplistByType(@RequestBody @ApiParam(name= "传入json格式",value="分页查询参数对象",required=true) GraphListReq req) {
        if(Strings.isNullOrEmpty(req.getType()))
            return SearchException.MISS_TYPE.get();

        try {
            Map<String, Object> data = graphService.getGroupList(req);
            return Wrapper.ok(data);
        } catch (Exception e) {
            log.error("", e);
            return Wrapper.error(e.getMessage());
        }
    }

    /**
     * @description 在企业列表通过族谱ID获取族谱详细信息
     * @param @RequestParam Long groupId
     * @return Wrapper
     * @author yuding
     * @date 2018/08/20
     */
    @RequestMapping(value = "/group_detail", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "在企业列表通过族谱ID获取族谱详细信息")
    public Wrapper getGroupDetailById(@RequestParam @ApiParam(name = "groupId",value = "族谱ID" , example = "123",required = false) Long groupId) {
        if(Strings.isNullOrEmpty(Long.toString(groupId)))
            return SearchException.MISS_GROUP_ID.get();
        try {
            Map<String,Object> groupDetailInfo = graphService.getGroupDetail(groupId);
            return Wrapper.ok(groupDetailInfo);
        } catch (Exception e) {
            log.error("", e);
            return Wrapper.error(e.getMessage());
        }
    }

    /**
     * 导入路径
     *
     * @param multipartFile
     * @param domainId
     * @param domain_name
     * @param type
     * @return
     */
//    @RequestMapping(value = "/import/path", method = RequestMethod.POST,
//            consumes = MediaType.MULTIPART_FORM_DATA, produces = MediaType.APPLICATION_JSON)
//	@NoneAuthorization
//    public Wrapper importPaths(
//            @RequestParam(value = "file", required = false) MultipartFile multipartFile,
//            @RequestParam(value = "domain_id", required = false) Long domainId,
//            @RequestParam(value = "domain_name", required = false) String domain_name,
//            @RequestParam(value = "type", required = false) String type) {
//        if (domainId == null && domain_name == null && type == null) {
//            return SearchException.MISS_DOMAIN.get();
//        }
//        try {
//            InputStream inputStream = multipartFile.getInputStream();
//            if (inputStream != null) {
//                if (domainId != null && domainId != 0) {
//                    graphClustersService.importPathsFromInput(inputStream, domainId);
//                } else if (domain_name != null && type != null) {
//                    graphClustersService.importPathsFromInput(inputStream, domain_name, type);
//                } else {
//                    return SearchException.WRONG_DOMAIN_COMB.get();
//                }
//            }
//        } catch (IOException e) {
//            return Wrapper.ERRORBuilder.data(e.getMessage()).build();
//        }
//        return Wrapper.OK;
//    }

    @Autowired
    private StockRightDataTransformer stockRightDataTransformer;

    /**
     * @author thomas
     * 股权结构
     *
     * @return
     */
    @ApiOperation(value = "查询指定企业的股权结构信息(对当前企业有三度股权关系的企业信息)")
    @RequestMapping(value = "/stockRight", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper stockRightStructure(@ApiParam(value = "公司名称", required = true) @RequestParam("name") String name)
    {
        if (Strings.isNullOrEmpty(name)) {
            return SearchException.MISS_COMPANY.get();
        }

        try {
            GraphVo graph = graphService.stockRight(name, GraphEdge.Direction.IN, 3);

            groupService.completeVertices(graph.getVertexes(),true);

            Tree tree = stockRightDataTransformer.graph2Tree(graph);
            
/*           try {
        	   //营业时间大于当前日期的，日期置为空
        	   Map<String, Object>  properties=tree.getProperties();
               Date operation_startdate=new SimpleDateFormat("yyyy-MM-dd").parse(String.valueOf(properties.get("operation_startdate")));
               if(operation_startdate.getTime()>new Date().getTime()){
               	properties.put("operation_startdate", "");
               }
			} catch (Exception e) {
			  
			}*/
            return Wrapper.OKBuilder.data(tree).build();
        } catch (Exception e) {
            log.error("", e);
            return Wrapper.ERRORBuilder.msg(e.getMessage()).build();
        }
    }

    /**
     * 对外投资
     * @param company
     * @return com.haizhi.iap.common.Wrapper
     * @author caochao
     * @Date 2018/8/28
     */
    @ApiOperation(value = "获取指定企业的对外投资树")
    @RequestMapping(value = "/outInvestWithTree", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    public Wrapper outInvest(@ApiParam(value = "企业名称", required = true) @RequestParam("company") String company) {
        if (Strings.isNullOrEmpty(company)) {
            return SearchException.MISS_COMPANY.get();
        }

        try {
            GraphVo graph = graphService.stockRight(company, GraphEdge.Direction.OUT, 3);

            groupService.completeVertices(graph.getVertexes(),true);

            Tree tree = stockRightDataTransformer.graph2Tree(graph, GraphEdge.Direction.OUT);
            return Wrapper.ok(tree);
        } catch (Exception e) {
            log.error("企业对外投资查询异常", e);
            return Wrapper.error(e.getMessage());
        }
    }

    @Autowired
    private MoneyFlowDataTransformer moneyFlowDataTransformer;

    /**
     * @author thomas
     * 资金往来
     *
     * @return
     */
    @RequestMapping(value = "/moneyFlow", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper moneyFlow(@RequestParam("name") String name)
    {
        if (Strings.isNullOrEmpty(name)) {
            return SearchException.MISS_COMPANY.get();
        }

        try {
            //add by linyong 建议:深度为1的双向就不要用travel了，直接筛选_from或者_to等于该公司的不就好了
            GraphVo graphVo = graphService.moneyFlow(name, GraphEdge.Direction.ALL, 1);
            Tree tree = moneyFlowDataTransformer.graph2Tree(graphVo);
            return Wrapper.OKBuilder.data(tree).build();
        } catch (Exception e) {
            log.error("", e);
            return Wrapper.ERRORBuilder.msg(e.getMessage()).build();
        }
    }

    @Autowired
    private CompanyGroupDataTransformer companyGroupDataTransformer;

    /**
     * @author thomas
     * 集团派系
     *
     * @return
     */
    @RequestMapping(value = "/group", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper companyGroup(@RequestParam("name") String name)
    {
        if (Strings.isNullOrEmpty(name)) {
            return SearchException.MISS_COMPANY.get();
        }

        try {
            GraphVo graphVo = graphService.companyGroup(name);
            graphVo = companyGroupDataTransformer.processGraph(graphVo);
            return Wrapper.OKBuilder.data(graphVo).build();
        } catch (Exception e) {
            log.error("", e);
            return Wrapper.ERRORBuilder.msg(e.getMessage()).build();
        }
    }

    @Autowired
    private OfficerDataTransformer officerDataTransformer;

    /**
     * @author thomas
     * 高管关系
     *
     * @return
     */
    @RequestMapping(value = "/officer", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper officer(@RequestParam("name") String name)
    {
        if (Strings.isNullOrEmpty(name)) {
            return SearchException.MISS_COMPANY.get();
        }

        try {
            GraphVo graphVo = graphService.officer(name, GraphEdge.Direction.IN, 1);
            Tree tree = officerDataTransformer.graph2Tree(graphVo);
            return Wrapper.OKBuilder.data(tree).build();
        } catch (Exception e) {
            log.error("", e);
            return Wrapper.ERRORBuilder.msg(e.getMessage()).build();
        }
    }

    @Autowired
    private InvestGroupDataTransformer investGroupDataTransformer;

    /**
     * @author thomas
     * 投资族谱
     *
     * @return
     */
    @RequestMapping(value = "/investGroup", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper investGroup(@RequestParam("name") String name)
    {
        if (Strings.isNullOrEmpty(name)) {
            return SearchException.MISS_COMPANY.get();
        }

        try {
            GraphVo graphVo = graphService.investGroup(name, GraphEdge.Direction.OUT, 1);
            Tree tree = investGroupDataTransformer.graph2Tree(graphVo);
            return Wrapper.OKBuilder.data(tree).build();
        } catch (Exception e) {
            log.error("", e);
            return Wrapper.ERRORBuilder.msg(e.getMessage()).build();
        }
    }

    @Autowired
    private EventDataTransformer eventDataTransformer;

    /**
     * @author thomas
     * 事件关联
     *
     * @return
     */
    @RequestMapping(value = "/event", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper event(@RequestParam("name") String name)
    {
        if (Strings.isNullOrEmpty(name)) {
            return SearchException.MISS_COMPANY.get();
        }

        try {
            GraphVo graphVo = graphService.event(name, 1);
            Tree tree = eventDataTransformer.graph2Tree(graphVo);
            return Wrapper.OKBuilder.data(tree).build();
        } catch (Exception e) {
            log.error("", e);
            return Wrapper.ERRORBuilder.msg(e.getMessage()).build();
        }
    }

    /**
     * @author thomas
     * 批量查annual_report
     *
     * @return
     */
    @RequestMapping(value = "/annual_report", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON)
    public Wrapper queryAnnualReport(@RequestBody @Valid AnnualReportQuery query)
    {
        String msg = query.validate();
        if(!StringUtils.isEmpty(msg))
            return Wrapper.ERRORBuilder.msg(msg).build();
        try {
            Map<String, List<Map<String, Object>>> annualReport = enterpriseRepo.getAnnualReport(query);
            return Wrapper.OKBuilder.data(annualReport).build();
        } catch (Exception e) {
            log.error("", e);
            return Wrapper.ERRORBuilder.msg(e.getMessage()).build();
        }
    }

    /**
     * 反洗钱
     *
     * @param company
     * @return
     */
    @RequestMapping(value = "/generateAmlGraph", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    public Wrapper generateAmlGraph(@RequestParam("company") String company) {
        if (Strings.isNullOrEmpty(company)) {
            return SearchException.MISS_COMPANY.get();
        }
        Graph graph = graphService.generateAmlGraph(company);
        return Wrapper.ok(graph);
    }
}
