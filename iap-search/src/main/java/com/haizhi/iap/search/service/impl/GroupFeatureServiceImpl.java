package com.haizhi.iap.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.haizhi.iap.common.utils.LogicDeleteUtil;
import com.haizhi.iap.search.conf.AppDataCollections;
import com.haizhi.iap.search.conf.BizDataCollections;
import com.haizhi.iap.search.constant.Fields;
import com.haizhi.iap.search.controller.model.DataItem;
import com.haizhi.iap.search.enums.GroupFeatureStatusEnum;
import com.haizhi.iap.search.model.NewRegisteredCompany;
import com.haizhi.iap.search.model.vo.GroupCreditEntityVo;
import com.haizhi.iap.search.model.vo.GroupFeatureVo;
import com.haizhi.iap.search.repo.CompanyGroupRepo;
import com.haizhi.iap.search.repo.GroupDetailRepo;
import com.haizhi.iap.search.repo.NewRegisteredCompanyRepo;
import com.haizhi.iap.search.service.GroupFeatureService;
import com.haizhi.iap.search.utils.ExchangeRateTransformer;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.elasticsearch.common.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xuguoqin
 * @date 2018/11/5 2:27 PM
 */
@Service
@Slf4j
public class GroupFeatureServiceImpl implements GroupFeatureService {

    @Autowired
    private CompanyGroupRepo companyGroupRepo;

    @Autowired
    private GroupDetailRepo groupDetailRepo;


    @Qualifier("appMongoDatabase")
    @Autowired
    private MongoDatabase appMongoDatabase;

    @Qualifier("bizMongoDatabase")
    @Autowired
    private MongoDatabase bizMongoDatabase;

    @Autowired
    ExchangeRateTransformer exchangeRateTransformer;

    @Autowired
    private NewRegisteredCompanyRepo newRegisteredCompanyRepo;

    /**
     * 1. 通过集团查询出企业列表
     * 2. 关联mongo聚合查询结果为对应类型的企业数和类型 计算出在集团中的占比
     * 3. 关联mongo中企业总数
     * 4. 构造返回结果
     *
     * @param groupName
     * @param type
     * @return
     */
    @Override
    public List<GroupFeatureVo> listGroupFeature(String groupName, String type) {
        String paths = companyGroupRepo.findByGroupName(groupName);
        if (StringUtils.isEmpty(paths)) {
            return new ArrayList<>();
        }
        Set<String> companySet = processPathsToCompanySet(paths);
        if (CollectionUtils.isEmpty(companySet)) {
            return new ArrayList<>();
        }
        List<Document> aggregateResult = getEnterpriseAggregateResult(companySet, type);

        Long groupTotalEnterpriseNum = getGroupTotalEnterpriseNum(companySet);
        if (groupTotalEnterpriseNum == 0) {
            return new ArrayList<>();
        }

        // 转换为视图对象 分类类型 集团该类型企业数量 该类型企业数量占该集团总企业比例
        // [Document{{_id=Document{{_id=批发业}}, count=2}}]
        return aggregateResult.stream()
                .filter(document -> document.get("_id") instanceof Document && Objects.nonNull(((Document) document.get("_id")).get("_id")) && document.get("count") instanceof Integer)
                .map(document -> {
                    GroupFeatureVo groupFeatureVO = new GroupFeatureVo();
                    groupFeatureVO.setContent(((Document) document.get("_id")).get("_id").toString());
                    Integer count = (Integer) document.get("count");
                    groupFeatureVO.setCount(count);
                    groupFeatureVO.setProportion(String.format("%.2f", count * 100.0 / groupTotalEnterpriseNum) + "%");
                    return groupFeatureVO;
                }).collect(Collectors.toList());
    }

    private Set<String> processPathsToCompanySet(String paths) {
        List<Map<String, Object>> convertPaths = JSON.parseObject(paths, new TypeReference<List<Map<String, Object>>>() {
        });
        Set<String> companySet = Sets.newHashSet();
        convertPaths.forEach(map -> {
            if (map.get("src_name") != null && map.get("src_belong_inner") != null && map.get("_from") != null && map.get("_from").toString().startsWith("Company")) {
                companySet.add(map.get("src_name").toString());
            }
            if (map.get("dst_name") != null && map.get("dst_belong_inner") != null && map.get("_to") != null && map.get("_to").toString().startsWith("Company")) {
                companySet.add(map.get("dst_name").toString());
            }
        });
        log.info("companySet: {}", companySet);
        return companySet;
    }

    /**
     * 处理paths的json数据 取出对应的map   key: 集团中的company value: page_rank  pagerank必须有值的映射
     *
     * @param paths
     * @return
     */
    private Map<String, String> processPathsToCompanyPageRankMap(String paths) {
        List<Map<String, Object>> convertPaths = JSON.parseObject(paths, new TypeReference<List<Map<String, Object>>>() {
        });
        Map<String, String> companyPageRankMap = Maps.newHashMap();
        convertPaths.forEach(map -> {
            if (map.get("src_name") != null && map.get("src_belong_inner") != null && map.get("_from") != null && map.get("src_page_rank") != null && map.get("_from").toString().startsWith("Company")) {
                companyPageRankMap.put(map.get("src_name").toString(), map.get("src_page_rank").toString());
            }
            if (map.get("dst_name") != null && map.get("dst_belong_inner") != null && map.get("_to") != null && map.get("dst_page_rank") != null && map.get("_to").toString().startsWith("Company")) {
                companyPageRankMap.put(map.get("dst_name").toString(), map.get("dst_page_rank").toString());
            }
        });
        log.info("companyPageRankMap: {}", companyPageRankMap);
        return companyPageRankMap;
    }

    /**
     * key company value id
     *
     * @param paths
     * @return
     */
    private Map<String, String> processPathsToCompanyIdMap(String paths) {
        List<Map<String, Object>> convertPaths = JSON.parseObject(paths, new TypeReference<List<Map<String, Object>>>() {
        });
        Map<String, String> companyIdMap = Maps.newHashMap();
        convertPaths.forEach(map -> {
            if (map.get("src_name") != null && map.get("src_belong_inner") != null && map.get("_from") != null && map.get("_from").toString().startsWith("Company")) {
                companyIdMap.put(map.get("src_name").toString(), map.get("_from").toString());
            }
            if (map.get("dst_name") != null && map.get("dst_belong_inner") != null && map.get("_to") != null && map.get("_to").toString().startsWith("Company")) {
                companyIdMap.put(map.get("dst_name").toString(), map.get("_to").toString());
            }
        });
        log.info("companyId: {}", companyIdMap);
        return companyIdMap;
    }

    /**
     * 得到企业总数
     *
     * @param companySet
     * @return
     */
    private Long getGroupTotalEnterpriseNum(Set<String> companySet) {
        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_ENTERPRISE_DATA_GOV);
        long count = collection.count(Filters.and(Filters.in("company", companySet), LogicDeleteUtil.addDeleteFileter()));
        log.info("groupTotalEnterpriseNum: {}", count);
        return count;
    }

    /**
     * 得到分组结果
     * 类型分组 企业数
     * mongo shell
     * db.enterprise_data_gov.aggregate(
     * {$match:{company:{$in:["万科","保利"]}}, logic_delete:{$ne:1}},
     * {$group:{_id:"$industry", count:{$sum:1}}},
     * {$sort:{count:-1}},
     * {$limit:5})
     *
     * @param
     * @return
     */
    public List<Document> getEnterpriseAggregateResult(Set<String> companySet, String type) {
        List<Document> resultList = new ArrayList<>();
        MongoCollection<Document> collection = appMongoDatabase.getCollection(AppDataCollections.COLL_ENTERPRISE_DATA_GOV);
        MongoCursor<Document> cursor = collection.aggregate(Lists.newArrayList(
                Aggregates.match(Filters.and(Filters.in("company", companySet), LogicDeleteUtil.addDeleteFileter())),
                Aggregates.group(buildGroupCondition(type), Accumulators.sum("count", 1)),
                Aggregates.sort(Sorts.descending("count")),
                Aggregates.match(Filters.ne("_id", null)),
                Aggregates.limit(5)
        )).iterator();

        try {
            while (cursor.hasNext()) {
                resultList.add(cursor.next());
            }
        } finally {
            cursor.close();
        }
        log.debug("resultList: {}", resultList);
        return resultList;
    }

    /**
     * 按照 行业 地域 分组条件
     *
     * @param type
     * @return
     */
    private Document buildGroupCondition(String type) {
        String groupIdOperator = GroupFeatureStatusEnum.getOperatorByType(type);
        if (Strings.isNullOrEmpty(groupIdOperator)) {
            throw new RuntimeException("no such type");
        }
        return new Document("_id", groupIdOperator);
    }


    @Override
    public DataItem listNewRegisterGroupEnterprise(String groupName, Integer type) {
        String paths = companyGroupRepo.findByGroupName(groupName);
        if (StringUtils.isEmpty(paths)) {
            return null;
        }
        Set<String> companySet = processPathsToCompanySet(paths);
        // map key: company value: pagerank 集团中所有的公司对应的pagerank
        Map<String, String> companyPageRankMap = processPathsToCompanyPageRankMap(paths);
        Map<String, String> companyIdMap = processPathsToCompanyIdMap(paths);
        // 集团中新注册公司列表
        List<NewRegisteredCompany> newRegisteredCompanyList = newRegisteredCompanyRepo.findByCompanyInGroupEnterprise(companySet, type);
        if (CollectionUtils.isEmpty(newRegisteredCompanyList)) {
            return null;
        }
        // 处理集团内新注册的企业按照pagerank降序 企业核心程度排序
        newRegisteredCompanyList = newRegisteredCompanyList.stream()
                .peek(newRegisteredCompany -> {
                    newRegisteredCompany.setPageRank(companyPageRankMap.get(newRegisteredCompany.getCompany()));
                    newRegisteredCompany.setId(companyIdMap.get(newRegisteredCompany.getCompany()));
                })
                .sorted(Comparator.comparing(NewRegisteredCompany::getPageRank, Comparator.nullsFirst(String::compareTo)).reversed())
                .collect(Collectors.toList());
        return new DataItem(newRegisteredCompanyList, (long) newRegisteredCompanyList.size());
    }

    @Override
    public List<GroupCreditEntityVo> findGroupCreditOverLimitEntities(String groupName, String type, Integer offset, Integer count) {
       //获取集团所属公司
        ArrayList<GroupCreditEntityVo> entities = new ArrayList<>();
        List<String> entitiesName = groupDetailRepo.findEntitiesNameByGNameTye(groupName, type);

        //设置mongo document 到pojo 转换的代码加解密器
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(pojoCodecProvider));

        MongoCollection<Document> collection = bizMongoDatabase.getCollection(BizDataCollections.COLL_M_ENT_INN_CUST_CREDIT_INF)
                .withCodecRegistry(pojoCodecRegistry);

        FindIterable<GroupCreditEntityVo> results = collection.find(Filters.and(Filters.in("cust_name",entitiesName)), GroupCreditEntityVo.class)
                .limit(count)
                .skip(offset);

        for (GroupCreditEntityVo result : results) {
            //改变币种成人民币
            changeToRMB(result);

            entities.add(result);
        }
        return entities;
    }

    private void changeToRMB(GroupCreditEntityVo result) {
        if (result != null) {
            Double amount = exchangeRateTransformer.fromOtherToRMB(result.getCreditCd(), result.getCreditLimit());
            result.setCreditCd(Fields.RMB);
            result.setCreditLimit(amount);
        }

    }

}
