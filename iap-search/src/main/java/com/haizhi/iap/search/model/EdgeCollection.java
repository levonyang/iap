package com.haizhi.iap.search.model;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class EdgeCollection {
    public final static EdgeCollection SUSPECTED = new EdgeCollection("suspected", GraphEdge.Direction.ALL, "");
    public final static EdgeCollection OFFICER = new EdgeCollection("officer", GraphEdge.Direction.ALL, "Company,Person");
    public final static EdgeCollection INVEST = new EdgeCollection("invest", GraphEdge.Direction.OUT, "Company,Person");
    public final static EdgeCollection SHAREHOLDER = new EdgeCollection("invest", GraphEdge.Direction.IN, "Company,Person", "shareholder");
    public final static EdgeCollection TRADABLE_SHARE = new EdgeCollection("tradable_share", GraphEdge.Direction.ALL, "Company,Person");
    public final static EdgeCollection PLAINTIFF = new EdgeCollection("plaintiff", GraphEdge.Direction.ALL, "");
    public final static EdgeCollection DEFENDANT = new EdgeCollection("defendant", GraphEdge.Direction.ALL, "");
    public final static EdgeCollection DISHONEST_EXECUTED = new EdgeCollection("dishonest_executed", GraphEdge.Direction.OUT, "");
    public final static EdgeCollection PUBLISH = new EdgeCollection("publish", GraphEdge.Direction.OUT, "");
    public final static EdgeCollection SUBMIT = new EdgeCollection("submit", GraphEdge.Direction.OUT, "");
    public final static EdgeCollection ACCUSED = new EdgeCollection("accused", GraphEdge.Direction.OUT, "");
    public final static EdgeCollection AGENT_BID = new EdgeCollection("agent_bid", GraphEdge.Direction.ALL, "Company,Bid_detail");
    public final static EdgeCollection WIN_BID = new EdgeCollection("win_bid", GraphEdge.Direction.ALL, "Company,Bid_detail");
    public final static EdgeCollection PUBLISH_BID = new EdgeCollection("publish_bid", GraphEdge.Direction.ALL, "Company,Bid_detail");
    public final static EdgeCollection ACTUAL_CONTROLLER = new EdgeCollection("actual_controller", GraphEdge.Direction.ALL, "Company,Person");
    public final static EdgeCollection FAMILY = new EdgeCollection("family", GraphEdge.Direction.ALL, "Person,Family_id");
    public final static EdgeCollection PERSON_MERGE_SUGGEST = new EdgeCollection("person_merge_suggest", GraphEdge.Direction.ALL, "Person");
    public final static EdgeCollection PERSON_MERGE = new EdgeCollection("person_merge", GraphEdge.Direction.ALL, "Person");
    public final static EdgeCollection CONCERT = new EdgeCollection("concert", GraphEdge.Direction.ALL, "Company,Person");
    public final static EdgeCollection SUE = new EdgeCollection("sue", GraphEdge.Direction.ALL, "Company,Person,Judge_process,Judgement_wenshu,Court_bulletin_doc,Court_announcement_doc");
    public final static EdgeCollection GUARANTEE = new EdgeCollection("guarantee", GraphEdge.Direction.ALL, "Company,Person");
    public final static EdgeCollection MONEY_FLOW = new EdgeCollection("money_flow", GraphEdge.Direction.ALL, "Company,Person");
    public final static EdgeCollection UPSTREAM = new EdgeCollection("upstream", GraphEdge.Direction.IN, "");
    public final static EdgeCollection CONTROL_SHAREHOLDER = new EdgeCollection("control_shareholder", GraphEdge.Direction.ALL, "Company,Person");
    public final static EdgeCollection COMPANY_GROUP = new EdgeCollection("company_group", GraphEdge.Direction.ALL, "Company");
    public final static EdgeCollection BELONG = new EdgeCollection("belong", GraphEdge.Direction.ALL, "Company");
    public final static EdgeCollection INDIRECT_INVEST = new EdgeCollection("indirect_invest", GraphEdge.Direction.IN, "Company,Person");
    /**
     * 中标(乙方) -> 招标(甲方)
     */
    public final static EdgeCollection PARTY_BID = new EdgeCollection("party_bid", GraphEdge.Direction.ALL, "Company");
    /**
     * 中标(乙方)
     */
    public final static EdgeCollection PARTY_BID_FROM = new EdgeCollection("party_bid", GraphEdge.Direction.OUT, "Company");
    /**
     * 招标(甲方)
     */
    public final static EdgeCollection PARTY_BID_TO = new EdgeCollection("party_bid", GraphEdge.Direction.IN, "Company");
    /**
     * 原告 -> 被告
     */
    public final static EdgeCollection SUE_RELATE = new EdgeCollection("sue_relate", GraphEdge.Direction.ALL, "Company,Person");
    /**
     * 被起诉
     */
    public final static EdgeCollection SUE_RELATE_FROM = new EdgeCollection("sue_relate", GraphEdge.Direction.OUT, "Company,Person");
    /**
     * 起诉
     */
    public final static EdgeCollection SUE_RELATE_TO = new EdgeCollection("sue_relate", GraphEdge.Direction.IN, "Company,Person");
    /**
     * 同为被告
     */
    public final static EdgeCollection PLAINTIFF_RELATE = new EdgeCollection("plaintiff_relate", GraphEdge.Direction.ALL, "Company,Person");
    /**
     * 同为原告
     */
    public final static EdgeCollection DEFENDANT_RELATE = new EdgeCollection("defendant_relate", GraphEdge.Direction.ALL, "Company,Person");
    /**
     * 共同提及
     */
    public final static EdgeCollection NEWS_ENTITY_RELATE = new EdgeCollection("news_entity_relate", GraphEdge.Direction.ALL, "Company,Person");
    /**
     * 疑似同一公司
     */
    public final static EdgeCollection SUSPECT_SAME_COMPANY = new EdgeCollection("suspect_same_company", GraphEdge.Direction.ALL, "Company");

    private static Map<String, EdgeCollection> edgeCollectionMap = new HashMap<>();

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
        edgeCollectionMap.put("SUE_RELATE_FROM", SUE_RELATE_FROM);
        edgeCollectionMap.put("SUE_RELATE_TO", SUE_RELATE_TO);
        edgeCollectionMap.put("PARTY_BID_FROM", PARTY_BID_FROM);
        edgeCollectionMap.put("PARTY_BID_TO", PARTY_BID_TO);
        edgeCollectionMap.put("INDIRECT_INVEST", INDIRECT_INVEST);
    }

    public static final String DEFAULT_VERTEX_COLLECTION = "Company,Person,Judge_process,Judgement_wenshu,Court_bulletin_doc,Court_announcement_doc";

    public EdgeCollection(String tableName, GraphEdge.Direction direction, String vertexCollections) {
        this.tableName = tableName;
        this.direction = direction;
        vertexCollection = StringUtils.split(vertexCollections, ',');
        this.filterName = tableName;
    }

    public EdgeCollection(String tableName, GraphEdge.Direction direction, String vertexCollections, String filterName) {
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

    public static EdgeCollection valueOf(String tableName) {
        return edgeCollectionMap.get(tableName);
    }

    public String getFilterName() {
        return filterName;
    }
}
