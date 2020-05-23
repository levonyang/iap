package com.haizhi.iap.search.conf;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Created by chenbo on 17/2/23.
 */
public class GraphLabelConf {
    private static Map<String, String> edgeLabelMap;

    private static Map<String, String> officerPositionLabelMap;

    static {
        {
            edgeLabelMap = Maps.newHashMap();
            edgeLabelMap.put("accused", "行政处罚");
            edgeLabelMap.put("court_announcement", "法院公告");
            edgeLabelMap.put("court_bulletin", "开庭公告");
            edgeLabelMap.put("defendant", "被告");
            edgeLabelMap.put("dishonest_execute_doc", "失信被执行文档");
            edgeLabelMap.put("dishonest_executed", "失信被执行");
            edgeLabelMap.put("invest", "投资");
            edgeLabelMap.put("judge_process_doc", "审判流程文档");
            edgeLabelMap.put("judgement_doc", "裁判文书");
            edgeLabelMap.put("legal_man", "法人");
            edgeLabelMap.put("officer", "高管");
            edgeLabelMap.put("penalty_doc", "行政处罚文档");
            edgeLabelMap.put("plaintiff", "原告");
            edgeLabelMap.put("publish", "发布");
            edgeLabelMap.put("shareholder", "股东");
            edgeLabelMap.put("submit", "提交专利");
            edgeLabelMap.put("guarantee", "担保");
            edgeLabelMap.put("money_flow", "转账");
            edgeLabelMap.put("suspected", "疑似");
            edgeLabelMap.put("agent_bid", "代理竞标");
            edgeLabelMap.put("publish_bid", "发布竞标");
            edgeLabelMap.put("win_bid", "竞标成功");
            edgeLabelMap.put("concert", "一致行动关系");
            edgeLabelMap.put("family", "亲属关系");
            edgeLabelMap.put("actual_control", "实际控制");
            edgeLabelMap.put("person_merge_suggest", "疑似可融合");
            edgeLabelMap.put("person_merge", "可融合");
        }

        {
            officerPositionLabelMap = Maps.newHashMap();
            officerPositionLabelMap.put("factory_director", "厂长");
            officerPositionLabelMap.put("board_director", "董事");
            officerPositionLabelMap.put("board_chairman", "董事长");
            officerPositionLabelMap.put("independent_director", "独立董事");
            officerPositionLabelMap.put("legal_man", "法人");
            officerPositionLabelMap.put("vice_chairman", "副董事长");
            officerPositionLabelMap.put("vice_manager", "副经理");
            officerPositionLabelMap.put("vice_general_manager", "副总经理");
            officerPositionLabelMap.put("principal", "负责人");
            officerPositionLabelMap.put("shareholder", "股东");
            officerPositionLabelMap.put("supervisor", "监事");
            officerPositionLabelMap.put("supervisory_chairman", "监事长");
            officerPositionLabelMap.put("manager", "经理");
            officerPositionLabelMap.put("syndic", "理事");
            officerPositionLabelMap.put("syndic_chairman", "理事长");
            officerPositionLabelMap.put("other_executive", "其他高管");
            officerPositionLabelMap.put("liquidation_group_member", "清算组成员");
            officerPositionLabelMap.put("liquidation_group_leader", "清算组负责人");
            officerPositionLabelMap.put("chief_representative", "首席代表");
            officerPositionLabelMap.put("invest", "投资");
            officerPositionLabelMap.put("admin_executive_director", "执行常务董事");
            officerPositionLabelMap.put("executive_director", "执行董事");
            officerPositionLabelMap.put("executive_supervisor", "执行监事");
            officerPositionLabelMap.put("executive_director", "执行理事");
            officerPositionLabelMap.put("employee_director", "职工董事");
            officerPositionLabelMap.put("employee_supervisor", "职工监事");
            officerPositionLabelMap.put("general_manager", "总经理");
        }
    }

    public static Map<String, String> getEdgeLabelMap() {
        return edgeLabelMap;
    }

    public static Map<String, String> getOfficerPositionLabelMap() {
        return officerPositionLabelMap;
    }
}
