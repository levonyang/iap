package com.haizhi.iap.search.repo;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.haizhi.iap.common.exception.ServiceAccessException;
import com.haizhi.iap.common.utils.CollectionUtil;
import com.haizhi.iap.common.utils.MapBuilder;
import com.haizhi.iap.search.constant.CommonFields;
import com.haizhi.iap.search.controller.GraphFoxxWS;
import com.haizhi.iap.search.controller.GraphWS;
import com.haizhi.iap.search.controller.model.GraphQuery;
import com.haizhi.iap.search.exception.SearchException;
import com.haizhi.iap.search.model.Concert;
import com.haizhi.iap.search.utils.NumberUtil;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.elasticsearch.common.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by chenbo on 2017/11/8.
 */
@Slf4j
@Repository
public class DetailGraphRepo {
    @Setter
    @Autowired
    GraphFoxxWS graphFoxxWS;

    @Setter
    @Autowired
    GraphWS graphWS;

    @Setter
    @Autowired
    EnterpriseRepo enterpriseRepo;

    /**
     * 获取实际控制人列表
     *
     * @param companyName
     * @return
     */
    public List<Map<String, Object>> getActualControlMan(String companyName) {
        try {
            Map<String, Object> company = getCompanyByName(companyName);
            List<Map<String, Object>> manList = Lists.newArrayList();
            if (company != null && company.containsKey("_id")) {
                String companyId = (String) company.get("_id");
                String aql = "WITH Company, Person FOR edge IN actual_controller \n" +
                        "FILTER edge._to == @companyId return {\"controller\": DOCUMENT(edge._from),\"edge\":edge}";
                Map<String, Object> bindVars = new MapBuilder().put("companyId", companyId).build();
                Map<String, Object> resp = graphWS.query(new GraphQuery(aql, bindVars));
                if (resp.get("error") instanceof Boolean && !(boolean) resp.get("error")) {
                    if (resp.get("result") instanceof List && ((List) resp.get("result")).size() > 0) {
                        manList = (List<Map<String, Object>>) resp.get("result");
                    }
                }
            }
            List<Map<String, Object>> actualControlManList = Lists.newArrayList();
            for (Map<String, Object> data : manList) {
                Map<String, Object> actualControlRes = null;
                Map<String, Object> actualControlEdge = null;
                if (data != null) {
                    if (data.get("controller") != null && data.get("controller") instanceof Map) {
                        actualControlRes = (Map<String, Object>) data.get("controller");
                    }
                    if (data.get("edge") != null && data.get("edge") instanceof Map) {
                        actualControlEdge = (Map<String, Object>) data.get("edge");
                    }
                }

                if (actualControlRes != null) {
                    Map<String, Object> man = Maps.newHashMap();
                    man.put("name", actualControlRes.get("name"));  // Person / Company都是name属性
                    man.put("id", actualControlRes.get("_id"));
                    if (actualControlEdge.get("rule") != null) {
                        man.put("rule", actualControlEdge.get("rule").toString());
                        man.put("depth", actualControlEdge.get("depth"));
                    }
                    actualControlManList.add(man);
                }
            }
            return actualControlManList;
        } catch (Exception ex) {
            log.error("{}", ex);
            throw new ServiceAccessException(SearchException.GRAPH_SERVER_ERROR);
        }
    }


    /**
     * 高管对外投资及任职
     *
     * @param companyName
     * @return
     */
    /*************单个keyPerson信息*************
    {
        "position": "总经理",
        "person": {
         "_key": "9D35DA2BE1C1115B282A6A962574E487",
            "_id": "Person/9D35DA2BE1C1115B282A6A962574E487",
            "_rev": "_X3uaCnW--J",
            "ctime": "2018-12-06 15:26:10",
            "name": "肖民",
            "utime": "2018-12-06 15:26:10"
       }
    }
    ===========转换为=======>
     {
         "position": "总经理",
         "person": {
             "_key": "9D35DA2BE1C1115B282A6A962574E487",
             "_id": "Person/9D35DA2BE1C1115B282A6A962574E487",
             "_rev": "_X3uaCnW--J",
             "ctime": "2018-12-06 15:26:10",
             "name": "肖民",
             "utime": "2018-12-06 15:26:10"
             }
        "key_person_position": "总经理",
        "key_person_name": "肖民",
        "id": "Person/9D35DA2BE1C1115B282A6A962574E487"
     }

     ***********************/
    public List<Map<String, Object>> getGraphKeyPerson(String companyName) {
        List<Map<String, Object>> graphKeyPersonList = getOfficerOfCompany(companyName);

        if (CollectionUtils.isEmpty(graphKeyPersonList)){
            return Collections.emptyList();
        }

        for (Map<String, Object> keyPerson : graphKeyPersonList) {
            keyPerson.put(CommonFields.KEY_PERSON_POSITION.getValue(), keyPerson.get("position"));
            if (keyPerson.get("person") instanceof Map) {
                keyPerson.put(CommonFields.KEY_PERSON_NAME.getValue(), ((Map) keyPerson.get("person")).get("name"));
                keyPerson.put(CommonFields.ID.getValue(), ((Map) keyPerson.get("person")).get("_id"));
            } else {
                keyPerson.put(CommonFields.KEY_PERSON_NAME.getValue(), keyPerson.get("person"));
            }
        }

        //以下为合并单人多职位的情况   董事长、董事 - 辛杰
        Map<String,Map<String, Object>> finalGraphKeyPersonList = new HashMap<>();

        Iterator<Map<String, Object>> iterator = graphKeyPersonList.iterator();
        while (iterator.hasNext()){
            Map<String, Object> currentPerson = iterator.next();
            String id = CollectionUtil.findMapValue(CommonFields.ID.getValue(),currentPerson);
            if (StringUtils.isBlank(id)){
                continue;
            }
            Map<String, Object> oldPerson = finalGraphKeyPersonList.get(id);
            if (CollectionUtils.isEmpty(oldPerson)){
                finalGraphKeyPersonList.put(id,currentPerson);
                continue;
            }

            //合并职位信息
            String oldPosition = CollectionUtil.findMapValue(CommonFields.KEY_PERSON_POSITION.getValue(),oldPerson);
            String newPosition = CollectionUtil.findMapValue(CommonFields.KEY_PERSON_POSITION.getValue(),currentPerson);
            if (!StringUtils.isBlank(newPosition)){
                newPosition = oldPosition  + "、"  + newPosition;
                oldPerson.put(CommonFields.KEY_PERSON_POSITION.getValue(),newPosition);
            }
        }

        return new ArrayList<>(finalGraphKeyPersonList.values());
    }

    /**
     * 股东对外投资及任职
     *
     * @param companyName
     * @return
     */
    public List<Map> getGraphContributor(String companyName) {
        Map<String, String> stockTypeMap = enterpriseRepo.getSectorMap(companyName); //查询公司的上市信息(股票代码)
        Map<String, Object> company = getCompanyByName(companyName);
        String companyId = null;
        List<Map<String, Object>> holdList = Lists.newArrayList();
        if (company != null && company.containsKey("_id")) {
            companyId = (String) company.get("_id");
            holdList = getHolding(companyId);
        }
        List<Map> graphContributorList;
        //如果公司已经上市
        if (stockTypeMap != null && stockTypeMap.values().size() > 0) {
            //上市公司
            graphContributorList = getTradableShareOfCompany(companyName);
            if(graphContributorList!= null && graphContributorList.size()>0){
                for (Map<String, Object> contributor : graphContributorList) {
                    if (contributor.get("person") instanceof Map) {
                        contributor.put("shareholder_name", ((Map) contributor.get("person")).get("name"));
                        contributor.put("id", ((Map) contributor.get("person")).get("_id"));
                        if (holdList != null) {
                            contributor.put("is_holding", isHolding(((Map) contributor.get("person")).get("name").toString(), holdList));
                        }
                    } else {
                        contributor.put("shareholder_name", contributor.get("person"));
                    }
                }
            }

        } else { //如果公司未上市
            graphContributorList = getInvest(companyName);
        }

        return graphContributorList;
    }

    /**
     * 获取投资信息
     * @param companyName
     * @return
     */
    public List<Map> getInvest(String companyName) {
        Map<String, Object> company = getCompanyByName(companyName);
        String companyId = null;
        List<Map<String, Object>> holdList = Lists.newArrayList();
        if (company != null && company.containsKey("_id")) {
            companyId = (String) company.get("_id");
            holdList = getHolding(companyId); //获取该公司的控股人列表
        }
        List<Map> graphContributorList = getInvestOfCompany(companyName);
        double total = 0;
        for (Map<String, Object> contributor : graphContributorList) {
            if (contributor.get("invest") != null && contributor.get("invest") instanceof Map) {
                if (((Map) contributor.get("invest")).get("invest_amount") != null) {
                    total += NumberUtil.tryParseDouble(((Map) contributor.get("invest")).get("invest_amount").toString());
                }
            }
            if (contributor.get("person") != null && contributor.get("person") instanceof Map) {
                Map<String, Object> person = (Map<String, Object>) contributor.get("person");
                if (holdList != null) {
                    contributor.put("is_holding", isHolding(person.get("name").toString(), holdList)); //判断该人员是否是控股
                }
                contributor.put("shareholder_name", person.get("name"));
                contributor.put("id", person.get("_id"));
            }
        }
        for (Map<String, Object> contributor : graphContributorList) {
            //算持股比例
            if (contributor.get("invest") != null && contributor.get("invest") instanceof Map) {
                Map<String, Object> invest = (Map<String, Object>) contributor.get("invest");
                contributor.putAll(invest);
                if (invest.get("invest_amount") != null && total != 0) {
                    double part = NumberUtil.tryParseDouble(invest.get("invest_amount").toString());
                    double percent = part / total;
                    BigDecimal decimal = new BigDecimal(percent * 100);
                    percent = decimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    //这里用decimalFormat会把0.7前面的0给截掉
                    contributor.put("shareholding_ratio", String.format("%.2f", percent) + "%");
                } else {
                    contributor.put("shareholding_ratio", "——");
                }
            }

        }
        return graphContributorList;
    }


    /**
     * 查询一致行动人
     * @param companyName
     * @return
     */
    public List<Map<String, Object>> getGraphConcert(String companyName) {
        List<Map<String, Object>> result = Lists.newArrayList();
        Map<String, Object> company = getCompanyByName(companyName);
        if (company != null && company.containsKey("_id")) {
            String companyId = (String) company.get("_id");
            List<Map<String, Object>> path = getConcert(companyId); //查询数据源获取一致行动人数据
            Pair<Map<String, Map<String, Object>>, Map<String, Map<String, Object>>> pair = splitPath(path);
            Map<String, Map<String, Object>> vertexMap = pair.getLeft();
            Map<String, Map<String, Object>> edgeMap = pair.getRight();

            for (String edgeKey : edgeMap.keySet()) {
                Concert target = new Concert();
                //一致行动对象
                Map<String, Object> edge = edgeMap.get(edgeKey);

                if (edge.get("target") != null) {
                    if (edge.get("target").toString().contains("/")) {
                        String targetId = edge.get("target").toString();
                        target.setId(targetId);
                        Map<String, Object> targetEntity = fetchDocument(targetId);
                        if (targetEntity != null) {
                            if (targetEntity.get("name") != null) {
                                target.setName(targetEntity.get("name").toString());
                            }
                        }
                    } else {
                        target.setName(edge.get("target").toString());
                    }
                }
                //一致行动人
                if (edge.get("_from") != null && !edge.get("_from").equals(companyId)) {
                    Concert person = new Concert();
                    person.setId(edge.get("_from").toString());
                    String name = "";
                    if (vertexMap.get(edge.get("_from").toString()) != null
                            && vertexMap.get(edge.get("_from").toString()).get("name") != null) {
                        name = vertexMap.get(edge.get("_from").toString()).get("name").toString();
                    }
                    person.setName(name);
                    if (edge.get("rule") != null) {
                        person.setRule(edge.get("rule").toString());
                    }
                    result.add(new MapBuilder().put("person", person).put("target", target).build());
                }
                if (!(edge.get("_to") == null) && !edge.get("_to").equals(companyId)) {
                    Concert person = new Concert();
                    person.setId(edge.get("_to").toString());
                    String name = "";
                    if (vertexMap.get(edge.get("_to").toString()) != null
                            && vertexMap.get(edge.get("_to").toString()).get("name") != null) {
                        name = vertexMap.get(edge.get("_to").toString()).get("name").toString();
                    }
                    person.setName(name);
                    if (edge.get("rule") != null) {
                        person.setRule(edge.get("rule").toString());
                    }
                    result.add(new MapBuilder().put("person", person).put("target", target).build());
                }
            }
            return result;
        } else {
            return null;
        }
    }

    private Map<String, Object> fetchDocument(String entityId) {
        if (Strings.isNullOrEmpty(entityId)) {
            return null;
        }
        try {
            String aql = "return document(@doc_id)";
            Map<String, Object> bindVars = new MapBuilder().put("doc_id", entityId).build();
            Map<String, Object> resp = graphWS.query(new GraphQuery(aql, bindVars));
            if (resp.get("error") instanceof Boolean && !(boolean) resp.get("error")) {
                if (resp.get("result") instanceof List && ((List) resp.get("result")).size() > 0) {
                    return ((List<Map<String, Object>>) resp.get("result")).get(0);
                }
            }
        } catch (Exception ex) {
            log.error("{}", ex);
            throw new ServiceAccessException(SearchException.GRAPH_SERVER_ERROR);
        }
        return null;
    }

    private List<Map<String, Object>> getConcert(String companyId) {
        try {
            if (companyId == null) {
                return null;
            }
            String aql = "WITH Company, Person FOR v, e, p IN 1..@depth ANY @startVertex concert " +
                    " RETURN p";
            Map<String, Object> bindVars = new MapBuilder()
                    .put("depth", 1)
                    .put("startVertex", companyId).build();
            Map<String, Object> resp = graphWS.query(new GraphQuery(aql, bindVars));
            if (resp.get("error") instanceof Boolean && !(boolean) resp.get("error")) {
                if (resp.get("result") instanceof List && ((List) resp.get("result")).size() > 0) {
                    return ((List<Map<String, Object>>) resp.get("result"));
                }
            }
        } catch (Exception ex) {
            log.error("{}", ex);
            throw new ServiceAccessException(SearchException.GRAPH_SERVER_ERROR);
        }
        return null;
    }

    private boolean isHolding(String name, List<Map<String, Object>> holdList) {
        if (name != null) {
            for (Map<String, Object> hold : holdList) {
                if (name.equals(hold.get("name"))) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<Map<String, Object>> getHolding(String companyId) {
        try {
            String aql = "WITH Company, Person For doc in control_shareholder filter doc._to == @companyId return DOCUMENT(doc._from)";
            Map<String, Object> bindVars = new MapBuilder().put("companyId", companyId).build();
            Map<String, Object> resp = graphWS.query(new GraphQuery(aql, bindVars));
            if (resp.get("error") instanceof Boolean && !(boolean) resp.get("error")) {
                if (resp.get("result") instanceof List) {
                    return ((List<Map<String, Object>>) resp.get("result"));
                }
            }
        } catch (Exception ex) {
            log.error("{}", ex);
            throw new ServiceAccessException(SearchException.GRAPH_SERVER_ERROR);
        }
        return null;
    }

    /**
    * @description 获取指定企业的高管信息
    * @param companyName
    * @return java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
    * @author liulu
    * @date 2018/12/25
    */
    /**************返回结果的样例数据 start **************
    [
        {
            "position": "总经理",
             "person": {
                    "_key": "9D35DA2BE1C1115B282A6A962574E487",
                    "_id": "Person/9D35DA2BE1C1115B282A6A962574E487",
                    "_rev": "_X3uaCnW--J",
                    "ctime": "2018-12-06 15:26:10",
                    "name": "肖民",
                    "utime": "2018-12-06 15:26:10"
                   }
        },
        {
            "position": "董事长",
             "person": {
                    "_key": "71CFA14EF7268837E2A59E30496E3035",
                    "_id": "Person/71CFA14EF7268837E2A59E30496E3035",
                    "_rev": "_X3vzzrG--D",
                    "ctime": "2018-12-06 15:26:10",
                    "name": "辛杰",
                    "utime": "2018-12-06 15:26:10"
                    }
        }
    ]
     ************返回结果的样例数据 end ********************/
    private List<Map<String, Object>> getOfficerOfCompany(String companyName) {
        try {
            Map<String, Object> company = getCompanyByName(companyName);
            if (company != null && company.containsKey("_id")) {
                String companyId = (String) company.get("_id");
                String aql = "WITH Company, Person For doc in officer filter doc._to == @companyId return {\"position\" : doc.position, \"person\":DOCUMENT(doc._from)}";
                Map<String, Object> bindVars = new MapBuilder().put("companyId", companyId).build();
                Map<String, Object> resp = graphWS.query(new GraphQuery(aql, bindVars));
                if (resp.get("error") instanceof Boolean && !(boolean) resp.get("error")) {
                    if (resp.get("result") instanceof List) {
                        return ((List<Map<String, Object>>) resp.get("result"));
                    }
                }
            }
        } catch (Exception ex) {
            log.error("{}", ex);
            throw new ServiceAccessException(SearchException.GRAPH_SERVER_ERROR);
        }
        return Collections.EMPTY_LIST;
    }

    private List<Map> getTradableShareOfCompany(String companyName) {
        try {
            Map<String, Object> company = getCompanyByName(companyName);
            if (company != null && company.containsKey("_id")) {
                String companyId = (String) company.get("_id");
                String aql = "WITH Company, Person For doc in tradable_share filter doc._to == @companyId return {\"shareholding_ratio\" : doc.total_stake_distribution, \"person\":DOCUMENT(doc._from)}";
                Map<String, Object> bindVars = new MapBuilder().put("companyId", companyId).build();
                Map<String, Object> resp = graphWS.query(new GraphQuery(aql, bindVars));
                if (resp.get("error") instanceof Boolean && !(boolean) resp.get("error")) {
                    if (resp.get("result") instanceof List) {
                        return ((List<Map>) resp.get("result"));
                    }
                }
            }
        } catch (Exception ex) {
            log.error("{}", ex);
            throw new ServiceAccessException(SearchException.GRAPH_SERVER_ERROR);
        }
        return null;
    }

    private List<Map> getInvestOfCompany(String companyName) {
        try {
            Map<String, Object> company = getCompanyByName(companyName);
            if (company != null && company.containsKey("_id")) {
                String companyId = (String) company.get("_id");
                String aql = "WITH Company, Person For doc in invest filter doc._to == @companyId return {\"invest\" : doc, \"person\":DOCUMENT(doc._from)}";
                Map<String, Object> bindVars = new MapBuilder().put("companyId", companyId).build();
                Map<String, Object> resp = graphWS.query(new GraphQuery(aql, bindVars));
                if (resp.get("error") instanceof Boolean && !(boolean) resp.get("error")) {
                    if (resp.get("result") instanceof List) {
                        return ((List<Map>) resp.get("result"));
                    }
                }
            }
        } catch (Exception ex) {
            log.error("{}", ex);
            throw new ServiceAccessException(SearchException.GRAPH_SERVER_ERROR);
        }
        return Collections.EMPTY_LIST;
    }

    private Map<String, Object> getCompanyByName(String company) {

        try {
            String aql = "With Company FOR doc IN Company FILTER doc.name == @name LIMIT 1 return doc ";
            Map<String, Object> bindVars = new MapBuilder().put("name", company).build();
            Map<String, Object> resp = graphWS.query(new GraphQuery(aql, bindVars));
            if (resp.get("error") instanceof Boolean && !(boolean) resp.get("error")) {
                if (resp.get("result") instanceof List && ((List) resp.get("result")).size() > 0) {
                    return ((List<Map<String, Object>>) resp.get("result")).get(0);
                }
            }
        } catch (Exception ex) {
            log.error("{}", ex);
            throw new ServiceAccessException(SearchException.GRAPH_SERVER_ERROR);
        }
        return null;
    }

    private Pair<Map<String, Map<String, Object>>, Map<String, Map<String, Object>>> splitPath(List<Map<String, Object>> result) {
        Map<String, Map<String, Object>> vertexMap = Maps.newHashMap();
        Map<String, Map<String, Object>> edgeMap = Maps.newHashMap();
        if (result != null) {
            for (Map<String, Object> resp : result) {
                if (resp.get("vertices") != null && resp.get("vertices") instanceof List) {
                    List<Map<String, Object>> nodes = (List<Map<String, Object>>) resp.get("vertices");
                    for (Map<String, Object> node : nodes) {
                        vertexMap.put((String) node.get("_id"), node);
                    }
                }

                if (resp.get("edges") != null && resp.get("edges") instanceof List) {
                    List<Map<String, Object>> edges = (List<Map<String, Object>>) resp.get("edges");
                    for (Map<String, Object> link : edges) {
                        edgeMap.put((String) link.get("_id"), link);
                    }
                }
            }
        }
        return Pair.of(vertexMap, edgeMap);
    }
}
