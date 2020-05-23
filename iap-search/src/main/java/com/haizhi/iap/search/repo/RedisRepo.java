package com.haizhi.iap.search.repo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.haizhi.iap.common.utils.SecretUtil;
import com.haizhi.iap.search.controller.model.FinancialReport;
import com.haizhi.iap.search.controller.model.Graph;
import com.haizhi.iap.search.controller.model.RegularReport;
import com.haizhi.iap.search.controller.model.Tree;
import com.haizhi.iap.search.enums.Keys;
import lombok.Setter;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;
import java.util.Map;

/**
 * Created by chenbo on 17/2/15.
 */
@Repository
public class RedisRepo {
    @Setter
    @Autowired
    private ObjectMapper objectMapper;

    @Setter
    @Autowired
    private JedisPool jedisPool;

    private static Integer CACHE_SECONDS = 5 * 60; //单位: 秒

    private static Integer CACHE_QUARTER_OF_DAY = 6 * 60 * 60;

    private static Integer CACHE_ONE_DAY = 24 * 60 * 60;

    public Tree  queryOverviewRelation(String companyName){
        return getCache(Keys.OVERVIEW_RELATION, companyName, Tree.class);
    }

    public void pushOverviewRelation(Tree overview, String companyName){
        pushCache(Keys.OVERVIEW_RELATION,companyName,overview,CACHE_QUARTER_OF_DAY);
    }

    public void pushBasic(Document doc, String companyName) {
        pushDocument(doc, companyName, Keys.BASIC);
    }

    public Document getBasic(String companyName) {
        return getDocument(companyName, Keys.BASIC);
    }

    public Document getListingInfo(String stockCode) {
        return getDocument(stockCode, Keys.LISTING_INFO);
    }

    public void pushListingInfo(Document doc, String stockCode) {
        pushDocument(doc, stockCode, Keys.LISTING_INFO);
    }

    public void pushFinancialReportBasic(Document doc, String companyName) {
        pushDocument(doc, companyName, Keys.FINANCIAL_REPORT_BASIC);
    }

    public Document getFinancialReportBasic(String companyName) {
        return getDocument(companyName, Keys.FINANCIAL_REPORT_BASIC);
    }

    public void pushFinancialReport(FinancialReport report, String stockCode) {
        pushCache(Keys.FINANCIAL_REPORT, stockCode, report, 10 * 60);
    }

    public FinancialReport getFinancialReport(String stockCode) {
        return getCache(Keys.FINANCIAL_REPORT, stockCode, FinancialReport.class);
    }

    public void pushCustomsInformation(Document doc, String companyName) {
        pushDocument(doc, companyName, Keys.CUSTOMS_INFORMATION);
    }

    public Document getCustomsInformation(String companyName) {
        return getDocument(companyName, Keys.CUSTOMS_INFORMATION);
    }

    public void pushTopTenSH(Document doc, String stockCode) {
        pushDocument(doc, stockCode, Keys.TOP_TEN_SH);
    }

    public Document getTopTenSH(String stockCode) {
        return getDocument(stockCode, Keys.TOP_TEN_SH);
    }

    public void pushRegularReport(RegularReport report, String stockCode) {
        pushCache(Keys.REGULAR_REPORT, stockCode, report, CACHE_SECONDS);
    }

    public RegularReport getRegularReport(String stockCode) {
        return getCache(Keys.REGULAR_REPORT, stockCode, RegularReport.class);
    }


    public void pushCount(Long value, String key, Keys keys) {
        pushCache(keys, key, value, CACHE_SECONDS, true);
    }

    public Long getCount(String key, Keys keys) {
        return getCache(keys, key, Long.class, true);
    }

    public void pushDocument(Document body, String companyName, Keys key) {
        pushCache(key, companyName, body, CACHE_SECONDS, true);
    }
    public Document getDocument(String companyName, Keys key) {
        return getCache(key, companyName, Document.class, true);
    }

    public void pushDocuments(List<Map> body, String keyValue, Keys key) {
        pushCache(key, keyValue, body, CACHE_SECONDS, true);
    }

    public List<Map> getDocuments(String keyValue, Keys key) {
        return getCache(key, keyValue, List.class, true);
    }

    public void pushNoticeCount(Long value, String stockCode) {
        pushCount(value, stockCode, Keys.NOTICE_COUNT);
    }

    public Long getNoticeCount(String stockCode) {
        return getCount(stockCode, Keys.NOTICE_COUNT);
    }

    public Long getRulesCount(String stockCode) {
        return getCount(stockCode, Keys.RULES_COUNT);
    }

    public void pushRulesCount(Long result, String stockCode) {
        pushCount(result, stockCode, Keys.RULES_COUNT);
    }

    public Long getBeingInvestedCount(String companyName) {
        return getCount(companyName, Keys.BEING_INVESTED_COUNT);
    }

    public void pushBeingInvestedCount(Long result, String companyName) {
        pushCount(result, companyName, Keys.BEING_INVESTED_COUNT);
    }

    public Long getInvestEventsCount(String companyName) {
        return getCount(companyName, Keys.INVEST_EVENTS_COUNT);
    }

    public void pushInvestEventsCount(Long result, String companyName) {
        pushCount(result, companyName, Keys.INVEST_EVENTS_COUNT);
    }

    public Long getPatentCount(String companyName) {
        return getCount(companyName, Keys.PATENT_COUNT);
    }

    public void pushPatentCount(Long result, String companyName) {
        pushCount(result, companyName, Keys.PATENT_COUNT);
    }

    public Long getBidInfoCount(String companyName) {
        return getCount(companyName, Keys.BID_INFO_COUNT);
    }

    public void pushBidInfoCount(Long result, String companyName) {
        pushCount(result, companyName, Keys.BID_INFO_COUNT);
    }

    public Long getWinBidCount(String companyName) {
        return getCount(companyName, Keys.WIN_BID_COUNT);
    }

    public void pushWinBidCount(Long result, String companyName) {
        pushCount(result, companyName, Keys.WIN_BID_COUNT);
    }

    public Long getCourtSessionAnnCount(String companyName) {
        return getCount(companyName, Keys.COURT_SESSION_ANN_COUNT);
    }

    public void pushCourtSessionAnnCount(Long result, String companyName) {
        pushCount(result, companyName, Keys.COURT_SESSION_ANN_COUNT);
    }

    public Long getCourtAnnCount(String companyName) {
        return getCount(companyName, Keys.COURT_ANN_COUNT);
    }

    public void pushCourtAnnCount(Long result, String companyName) {
        pushCount(result, companyName, Keys.COURT_ANN_COUNT);
    }

    public Long getTaxRankCount(String companyName) {
        return getCount(companyName, Keys.TAX_RANK_COUNT);
    }

    public void pushTaxRankCount(Long result, String companyName) {
        pushCount(result, companyName, Keys.TAX_RANK_COUNT);
    }

    public Long getJudgeProcessCount(String companyName) {
        return getCount(companyName, Keys.JUDGE_PROCESS_COUNT);
    }

    public void pushJudgeProcessCount(Long result, String companyName) {
        pushCount(result, companyName, Keys.JUDGE_PROCESS_COUNT);
    }

    public Long getJudgementCount(String companyName) {
        return getCount(companyName, Keys.JUDGEMENT_COUNT);
    }

    public void pushJudgementCount(Long result, String companyName) {
        pushCount(result, companyName, Keys.JUDGEMENT_COUNT);
    }

    public Long getOwingTaxCount(String companyName) {
        return getCount(companyName, Keys.OWING_TAX_COUNT);
    }

    public void pushOwingTaxCount(Long result, String companyName) {
        pushCount(result, companyName, Keys.OWING_TAX_COUNT);
    }

    public Long getPenaltyCount(String companyName) {
        return getCount(companyName, Keys.PENALTY_COUNT);
    }

    public void pushPenaltyCount(Long result, String companyName) {
        pushCount(result, companyName, Keys.PENALTY_COUNT);
    }

    public Long getDishonestCount(String companyName) {
        return getCount(companyName, Keys.DISHONEST_COUNT);
    }

    public void pushDishonestCount(Long result, String companyName) {
        pushCount(result, companyName, Keys.DISHONEST_COUNT);
    }

    public Long getExecutionCount(String companyName) {
        return getCount(companyName, Keys.EXECUTION_COUNT);
    }

    public void pushExecutionCount(Long result, String companyName) {
        pushCount(result, companyName, Keys.EXECUTION_COUNT);
    }

    public Map<String, Number> getBaiduNewsCountMap(String companyName) {
        return getCache(Keys.BAIDUNEWS_COUNTMAP, companyName, Map.class, true);
    }

    public void pushBaiduNewsCountMap(Map<String, Number> result, String companyName) {
        pushCache(Keys.BAIDUNEWS_COUNTMAP, companyName, result, CACHE_SECONDS, true);
    }

    public Long getFinancialEventsCount(String companyName) {
        return getCount(companyName, Keys.FINANCIAL_EVENTS_COUNT);
    }

    public void pushFinancialEventsCount(Long result, String companyName) {
        pushCount(result, companyName, Keys.FINANCIAL_EVENTS_COUNT);
    }

    public Long getAcquirerEventsCount(String companyName) {
        return getCount(companyName, Keys.ACQUIRER_EVENTS_COUNT);
    }

    public void pushAcquirerEventsCount(Long result, String companyName) {
        pushCount(result, companyName, Keys.ACQUIRER_EVENTS_COUNT);
    }

    public Long getAcquireredEventsCount(String companyName) {
        return getCount(companyName, Keys.ACQUIRERED_EVENTS_COUNT);
    }

    public void pushAcquireredEventsCount(Long result, String companyName) {
        pushCount(result, companyName, Keys.ACQUIRERED_EVENTS_COUNT);
    }

    public Long getExitEventsCount(String companyName) {
        return getCount(companyName, Keys.EXIT_EVENTS_COUNT);
    }

    public void pushExitEventsCount(Long result, String companyName) {
        pushCount(result, companyName, Keys.EXIT_EVENTS_COUNT);
    }

    public Long getLandAuctionCount(String companyName) {
        return getCount(companyName, Keys.LAND_AUCTION_COUNT);
    }

    public void pushLandAuctionCount(Long result, String companyName) {
        pushCount(result, companyName, Keys.LAND_AUCTION_COUNT);
    }

    public Document getInvestInstitution(String companyName) {
        return getDocument(companyName, Keys.INVEST_INSTITUTION);
    }

    public void pushInvestInstitution(Document doc, String companyName) {
        pushDocument(doc, companyName, Keys.INVEST_INSTITUTION);
    }


    public String getGraphClusters(String cid, Long domainId) {
        return hgetCache(Keys.GRAPH_CLUSTERS, String.valueOf(domainId), cid, String.class);
    }


    /**
    * @description 从缓存中获取单个族谱信息
    * @param groupName 族谱名称
    * @param type  族谱类型
    * @return java.lang.String 族谱信息
    * @author LewisLouis
    * @date 2018/8/20
    */
    public String getGraphGroup(String groupName, String type) {
        return hgetCache(Keys.GRAPH_GROUPS, String.valueOf(type), groupName, String.class);
    }

    public void setGraphClusters(String cid, Long domainId, String value) {
        hsetCache(Keys.GRAPH_CLUSTERS, String.valueOf(domainId), cid, value);
    }


    /**
    * @description 向缓存中存入单个族谱信息
    * @param groupName 族谱名称
    * @param type 族谱类型
    * @param value  族谱信息
    * @return void
    * @author LewisLouis
    * @date 2018/8/20
    */
    public void setGraphGroup(String groupName, String type, String value){
        hsetCache(Keys.GRAPH_GROUPS,String.valueOf(type),groupName,value);
    }

    public void deleteGraphClusters(Long domainId) {
        deleteCache(Keys.GRAPH_CLUSTERS, String.valueOf(domainId));
    }


    /**
    * @description 从缓存中删除一类族谱信息
    * @param type 族谱类型
    * @return void
    * @author LewisLouis
    * @date 2018/8/20
    */
    public void deleteGraphGroups(String type){

        deleteCache(Keys.GRAPH_GROUPS,String.valueOf(type));

    }

    public Graph getGraphCache(String companyId) {
        return getCache(Keys.GRAPH_COMPANY, companyId, Graph.class);
    }

    public void pushGraphCache(String companyId, Graph graph) {
        pushCache(Keys.GRAPH_COMPANY, companyId, graph, CACHE_QUARTER_OF_DAY);
    }

    public Map<String, Double> getExchangeRateCache() {
        return getCache(Keys.EXCHANGE_RATE, Map.class);
    }

    public void pushExchangeRateCache(Map<String, Double> rateList) {
        pushCache(Keys.EXCHANGE_RATE, rateList, CACHE_ONE_DAY);
    }

    public Map<Integer, Map<String, Object>> getEnvironmentProtection(String companyName) {
        return getCache(Keys.ENVIRONMENT_PROTECTION, companyName, Map.class, true);
    }

    public void pushEnvironmentProtection(Map<Integer, Map<String, Object>> map, String companyName) {
        pushCache(Keys.ENVIRONMENT_PROTECTION, companyName, map, CACHE_SECONDS, true);
    }

    public List getGraphConcert(String companyName) {
        return getCache(Keys.GRAPH_CONCERT, companyName, List.class, true);
    }

    public void pushGraphConcert(List concertList, String companyName) {
        pushCache(Keys.GRAPH_CONCERT, companyName, concertList, CACHE_QUARTER_OF_DAY, true);
    }

    public List getGraphContributor(String companyName) {
        return getCache(Keys.GRAPH_CONTRIBUTOR, companyName, List.class, true);
    }

    public void pushGraphContributor(List contributerList, String companyName) {
        pushCache(Keys.GRAPH_CONTRIBUTOR, companyName, contributerList, CACHE_QUARTER_OF_DAY, true);
    }

    public List getGraphKeyPerson(String companyName) {
        return getCache(Keys.GRAPH_KEY_PERSON, companyName, List.class, true);
    }

    public void pushGraphKeyPerson(List keyPersonList, String companyName) {
        pushCache(Keys.GRAPH_KEY_PERSON, companyName, keyPersonList, CACHE_QUARTER_OF_DAY, true);
    }

    /**
     * 通用的写入缓存的方法
     *
     * @param keys      缓存的前缀
     * @param keyParam  缓存的标识符
     * @param value     缓存值
     * @param cacheTime 缓存时间
     * @param shouldMd5 keyParam是否需要md5
     * @return void
     * @author caochao
     * @Date 2018/8/9
     */
    private <T> void pushCache(Keys keys, String keyParam, T value, int cacheTime, boolean shouldMd5) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String redisKey = null;
            if (keyParam == null) {
                redisKey = keys.get();
            } else {
                redisKey = keys.get(shouldMd5 ? SecretUtil.md5(keyParam) : keyParam);
            }

            jedis.setex(redisKey, cacheTime, objectMapper.writeValueAsString(value));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                try {
                    jedis.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private <T> void pushCache(Keys keys, String keyParam, T value, int cacheTime) {
        pushCache(keys, keyParam, value, cacheTime, false);
    }

    private <T> void pushCache(Keys keys, T value, int cacheTime) {
        pushCache(keys, null, value, cacheTime, false);
    }

    /**
     * 通用的获取缓存的方法
     *
     * @param keys      缓存前缀
     * @param keyParam  缓存标识
     * @param clazz     缓存类型
     * @param shouldMd5 keyParam是否需要md5
     * @return T
     * @author caochao
     * @Date 2018/8/9
     */
    private <T> T getCache(Keys keys, String keyParam, Class<T> clazz, boolean shouldMd5) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String redisKey = null;
            if (keyParam == null) {
                redisKey = keys.get();
            } else {
                redisKey = keys.get(shouldMd5 ? SecretUtil.md5(keyParam) : keyParam);
            }
            String response = jedis.get(redisKey);
            return Strings.isNullOrEmpty(response) ? null : objectMapper.readValue(response, clazz);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                try {
                    jedis.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return null;
    }

    private <T> T getCache(Keys keys, String keyParam, Class<T> clazz) {
        return getCache(keys, keyParam, clazz, false);
    }

    private <T> T getCache(Keys keys, Class<T> clazz) {
        return getCache(keys, null, clazz, false);
    }

    private void deleteCache(Keys keys, String keyValue) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String key = keys.get(keyValue);
            jedis.del(key);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (jedis != null) {
                try {
                    jedis.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private <T> T hgetCache(Keys keys, String keyParam, String hashKey, Class<T> clazz, boolean shouldMd5) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String redisKey = null;
            if (keyParam == null) {
                redisKey = keys.get();
            } else {
                redisKey = keys.get(shouldMd5 ? SecretUtil.md5(keyParam) : keyParam);
            }
            String response = jedis.hget(redisKey, hashKey);
            return Strings.isNullOrEmpty(response) ? null : objectMapper.readValue(response, clazz);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                try {
                    jedis.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return null;
    }

    private <T> T hgetCache(Keys keys, String keyParam, String hashKey, Class<T> clazz) {
        return hgetCache(keys, keyParam, hashKey, clazz, false);
    }

    private <T> void hsetCache(Keys keys, String keyParam, String hashKey, T value, boolean shouldMd5) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String redisKey = null;
            if (keyParam == null) {
                redisKey = keys.get();
            } else {
                redisKey = keys.get(shouldMd5 ? SecretUtil.md5(keyParam) : keyParam);
            }

            jedis.hset(redisKey, hashKey, objectMapper.writeValueAsString(value));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                try {
                    jedis.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private <T> void hsetCache(Keys keys, String keyParam, String hashKey, T value) {
        hsetCache(keys, keyParam, hashKey, value, false);
    }
}
