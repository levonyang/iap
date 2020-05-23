package com.haizhi.iap.mobile.conf;

import com.haizhi.iap.mobile.bean.normal.GraphEdge;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class ArangoEdgeConf
{
    public final static ArangoEdgeConf SUSPECTED = new ArangoEdgeConf("suspected", GraphEdge.Direction.ALL, "");
    public final static ArangoEdgeConf OFFICER = new ArangoEdgeConf("officer", GraphEdge.Direction.ALL, "Company,Person");
    public final static ArangoEdgeConf INVEST = new ArangoEdgeConf("invest", GraphEdge.Direction.OUT, "Company,Person");
    public final static ArangoEdgeConf SHAREHOLDER = new ArangoEdgeConf("invest", GraphEdge.Direction.IN, "Company,Person", "shareholder");
    public final static ArangoEdgeConf TRADABLE_SHARE = new ArangoEdgeConf("tradable_share", GraphEdge.Direction.ALL, "Company,Person");
    public final static ArangoEdgeConf PLAINTIFF = new ArangoEdgeConf("plaintiff", GraphEdge.Direction.ALL, "");
    public final static ArangoEdgeConf DEFENDANT = new ArangoEdgeConf("defendant", GraphEdge.Direction.ALL, "");
    public final static ArangoEdgeConf DISHONEST_EXECUTED = new ArangoEdgeConf("dishonest_executed", GraphEdge.Direction.OUT, "");
    public final static ArangoEdgeConf PUBLISH = new ArangoEdgeConf("publish", GraphEdge.Direction.OUT, "");
    public final static ArangoEdgeConf SUBMIT = new ArangoEdgeConf("submit", GraphEdge.Direction.OUT, "");
    public final static ArangoEdgeConf ACCUSED = new ArangoEdgeConf("accused", GraphEdge.Direction.OUT, "");
    public final static ArangoEdgeConf AGENT_BID = new ArangoEdgeConf("agent_bid", GraphEdge.Direction.ALL, "Company,Bid_detail");
    public final static ArangoEdgeConf WIN_BID = new ArangoEdgeConf("win_bid", GraphEdge.Direction.ALL, "Company,Bid_detail");
    public final static ArangoEdgeConf PUBLISH_BID = new ArangoEdgeConf("publish_bid", GraphEdge.Direction.ALL, "Company,Bid_detail");
    public final static ArangoEdgeConf ACTUAL_CONTROLLER = new ArangoEdgeConf("actual_controller", GraphEdge.Direction.ALL, "Company,Person");
    public final static ArangoEdgeConf FAMILY = new ArangoEdgeConf("family", GraphEdge.Direction.ALL, "Person,Family_id");
    public final static ArangoEdgeConf PERSON_MERGE_SUGGEST = new ArangoEdgeConf("person_merge_suggest", GraphEdge.Direction.ALL, "Person");
    public final static ArangoEdgeConf PERSON_MERGE = new ArangoEdgeConf("person_merge", GraphEdge.Direction.ALL, "Person");
    public final static ArangoEdgeConf CONCERT = new ArangoEdgeConf("concert", GraphEdge.Direction.ALL, "Company,Person");
    public final static ArangoEdgeConf SUE = new ArangoEdgeConf("sue", GraphEdge.Direction.ALL, "Company,Person,Judge_process,Judgement_wenshu,Court_bulletin_doc,Court_announcement_doc");
    public final static ArangoEdgeConf GUARANTEE = new ArangoEdgeConf("guarantee", GraphEdge.Direction.ALL, "Company,Person");
    public final static ArangoEdgeConf MONEY_FLOW = new ArangoEdgeConf("money_flow", GraphEdge.Direction.ALL, "Company,Person");
    public final static ArangoEdgeConf UPSTREAM = new ArangoEdgeConf("upstream", GraphEdge.Direction.IN, "");
    public final static ArangoEdgeConf CONTROL_SHAREHOLDER = new ArangoEdgeConf("control_shareholder", GraphEdge.Direction.ALL, "Company,Person");
    public final static ArangoEdgeConf COMPANY_GROUP = new ArangoEdgeConf("company_group", GraphEdge.Direction.ALL, "Company");
    public final static ArangoEdgeConf BELONG = new ArangoEdgeConf("belong", GraphEdge.Direction.ALL, "Company");
    /**
     * 中标(乙方) -> 招标(甲方)
     */
    public final static ArangoEdgeConf PARTY_BID = new ArangoEdgeConf("party_bid", GraphEdge.Direction.ALL, "Company");
    /**
     * 原告 -> 被告
     */
    public final static ArangoEdgeConf SUE_RELATE = new ArangoEdgeConf("sue_relate", GraphEdge.Direction.ALL, "Company,Person");
    /**
     * 同为被告
     */
    public final static ArangoEdgeConf PLAINTIFF_RELATE = new ArangoEdgeConf("plaintiff_relate", GraphEdge.Direction.ALL, "Company,Person");
    /**
     * 同为原告
     */
    public final static ArangoEdgeConf DEFENDANT_RELATE = new ArangoEdgeConf("defendant_relate", GraphEdge.Direction.ALL, "Company,Person");
    /**
     * 共同提及
     */
    public final static ArangoEdgeConf NEWS_ENTITY_RELATE = new ArangoEdgeConf("news_entity_relate", GraphEdge.Direction.ALL, "Company,Person");
    /**
     * 疑似同一公司
     */
    public final static ArangoEdgeConf SUSPECT_SAME_COMPANY = new ArangoEdgeConf("suspect_same_company", GraphEdge.Direction.ALL, "Company");

    private static Map<String, ArangoEdgeConf> edgeCollectionMap = new HashMap<>();

    static {
        edgeCollectionMap.put("SUSPECTED", SUSPECTED);
        edgeCollectionMap.put("OFFICER", OFFICER);
        edgeCollectionMap.put("INVEST", INVEST);
        edgeCollectionMap.put("SHAREHOLDER", SHAREHOLDER);
        edgeCollectionMap.put("TRADABLE_SHARE", TRADABLE_SHARE);
        edgeCollectionMap.put("PLAINTIFF", PLAINTIFF);
        edgeCollectionMap.put("DEFENDANT", DEFENDANT);
        edgeCollectionMap.put("DISHONEST_EXECUTED", DISHONEST_EXECUTED);
        edgeCollectionMap.put("PUBLISH", PUBLISH);
        edgeCollectionMap.put("SUBMIT", SUBMIT);
        edgeCollectionMap.put("ACCUSED", ACCUSED);
        edgeCollectionMap.put("AGENT_BID", AGENT_BID);
        edgeCollectionMap.put("WIN_BID", WIN_BID);
        edgeCollectionMap.put("PUBLISH_BID", PUBLISH_BID);
        edgeCollectionMap.put("ACTUAL_CONTROLLER", ACTUAL_CONTROLLER);
        edgeCollectionMap.put("FAMILY", FAMILY);
        edgeCollectionMap.put("PERSON_MERGE_SUGGEST", PERSON_MERGE_SUGGEST);
        edgeCollectionMap.put("PERSON_MERGE", PERSON_MERGE);
        edgeCollectionMap.put("CONCERT", CONCERT);
        edgeCollectionMap.put("SUE", SUE);
        edgeCollectionMap.put("GUARANTEE", GUARANTEE);
        edgeCollectionMap.put("MONEY_FLOW", MONEY_FLOW);
        edgeCollectionMap.put("UPSTREAM", UPSTREAM);
        edgeCollectionMap.put("CONTROL_SHAREHOLDER", CONTROL_SHAREHOLDER);
        edgeCollectionMap.put("COMPANY_GROUP", COMPANY_GROUP);
        edgeCollectionMap.put("BELONG", BELONG);
        edgeCollectionMap.put("PARTY_BID", PARTY_BID);
        edgeCollectionMap.put("SUE_RELATE", SUE_RELATE);
        edgeCollectionMap.put("PLAINTIFF_RELATE", PLAINTIFF_RELATE);
        edgeCollectionMap.put("DEFENDANT_RELATE", DEFENDANT_RELATE);
        edgeCollectionMap.put("NEWS_ENTITY_RELATE", NEWS_ENTITY_RELATE);
        edgeCollectionMap.put("SUSPECT_SAME_COMPANY", SUSPECT_SAME_COMPANY);
    }

    public static final String DEFAULT_VERTEX_COLLECTION = "Company,Person,Judge_process,Judgement_wenshu,Court_bulletin_doc,Court_announcement_doc";

    public ArangoEdgeConf(String tableName, GraphEdge.Direction direction, String vertexCollections) {
        this.tableName = tableName;
        this.direction = direction;
        vertexCollection = StringUtils.split(vertexCollections, ',');
        this.filterName = tableName;
    }

    public ArangoEdgeConf(String tableName, GraphEdge.Direction direction, String vertexCollections, String filterName) {
        this.tableName = tableName;
        this.direction = direction;
        vertexCollection = StringUtils.split(vertexCollections, ',');
        this.filterName = filterName;
    }

    private String filterName;
    private String tableName;
    private GraphEdge.Direction direction;
    private String[] vertexCollection;

    public String getTableName() {
        return tableName;
    }

    public GraphEdge.Direction getDirection() {
        return direction;
    }

    public String[] getVertexCollection() {
        return vertexCollection;
    }

    public static ArangoEdgeConf valueOf(String tableName) {
        return edgeCollectionMap.get(tableName);
    }

    public String getFilterName() {
        return filterName;
    }
}
