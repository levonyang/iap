package com.haizhi.iap.mobile.conf;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by thomas on 18/3/26.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraphEdgeLabel
{
    public static final String ZERO_PERCENTAGE = "0%";

    private String table;
    /**
     * 该field的值会做为label
     */
    private String field;
    /**
     * 表的中文名
     */
    private String tableCnName;

    public static Map<String, GraphEdgeLabel> EDGE_LABEL_CONF_MAP = new HashMap<>();

    static {
        EDGE_LABEL_CONF_MAP.put("accused", new GraphEdgeLabel("accused", null, "行政处罚"));
        EDGE_LABEL_CONF_MAP.put("court_announcement", new GraphEdgeLabel("court_announcement", null, "法院公告"));
        EDGE_LABEL_CONF_MAP.put("tradable_share", new GraphEdgeLabel("tradable_share", "total_stake_distribution", "投资"));
        EDGE_LABEL_CONF_MAP.put("invest", new GraphEdgeLabel("invest", "invest_amount", "投资"));
        EDGE_LABEL_CONF_MAP.put("officer", new GraphEdgeLabel("officer", "position", "高管"));
        EDGE_LABEL_CONF_MAP.put("belong", new GraphEdgeLabel("belong", null, "分支机构"));
        EDGE_LABEL_CONF_MAP.put("party_bid", new GraphEdgeLabel("party_bid", null, "招标中标"));
        EDGE_LABEL_CONF_MAP.put("sue_relate", new GraphEdgeLabel("sue_relate", null, "原告被告"));
        EDGE_LABEL_CONF_MAP.put("plaintiff_relate", new GraphEdgeLabel("plaintiff_relate", null, "同为原告"));
        EDGE_LABEL_CONF_MAP.put("guarantee", new GraphEdgeLabel("guarantee", null, "担保"));
        EDGE_LABEL_CONF_MAP.put("money_flow", new GraphEdgeLabel("money_flow", "money", "转账"));
        EDGE_LABEL_CONF_MAP.put("defendant_relate", new GraphEdgeLabel("defendant_relate", null, "同为被告"));
        EDGE_LABEL_CONF_MAP.put("news_entity_relate", new GraphEdgeLabel("news_entity_relate", null, "共同提及"));
        EDGE_LABEL_CONF_MAP.put("publish_bid", new GraphEdgeLabel("publish_bid", null, "发布竞标"));
        EDGE_LABEL_CONF_MAP.put("win_bid", new GraphEdgeLabel("win_bid", null, "竞标成功"));
        EDGE_LABEL_CONF_MAP.put("concert", new GraphEdgeLabel("concert", null, "一致行动关系"));
        EDGE_LABEL_CONF_MAP.put("family", new GraphEdgeLabel("family", null, "亲属关系"));
        EDGE_LABEL_CONF_MAP.put("actual_control", new GraphEdgeLabel("actual_control", null, "间接控制"));
        EDGE_LABEL_CONF_MAP.put("control_shareholder", new GraphEdgeLabel("control_shareholder", null, "控股股东"));
        EDGE_LABEL_CONF_MAP.put("suspect_same_company", new GraphEdgeLabel("suspect_same_company", null, "地址电话相同"));
    }
}
