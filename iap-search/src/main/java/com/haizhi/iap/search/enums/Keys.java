package com.haizhi.iap.search.enums;

/**
 * Created by chenbo on 17/2/15.
 */
public enum Keys {
    BASIC, ANNUAL_REPORT, LISTING_INFO, FINANCIAL_REPORT_BASIC, FINANCIAL_REPORT, NOTICE, TOP_TEN_SH, RULES,

    REGULAR_REPORT, BEING_INVESTED, INVEST_EVENTS, FINANCIAL_EVENTS, ACQUIRER_EVENTS, ACQUIRERED_EVENTS, EXIT_EVENTS,

    PATENT, BID_INFO, WIN_BID, LAND_AUCTION, COURT_SESSION_ANN, COURT_ANN, TAX_RANK,

    JUDGE_PROCESS, JUDGEMENT, OWING_TAX, PENALTY, DISHONEST, EXECUTION, SENTIMENT, BAIDUNEWS, INVEST_INSTITUTION,

    NOTICE_COUNT, RULES_COUNT, BEING_INVESTED_COUNT, INVEST_EVENTS_COUNT, FINANCIAL_EVENTS_COUNT, ACQUIRER_EVENTS_COUNT,

    ACQUIRERED_EVENTS_COUNT, EXIT_EVENTS_COUNT, PATENT_COUNT, BID_INFO_COUNT, WIN_BID_COUNT, LAND_AUCTION_COUNT,

    COURT_SESSION_ANN_COUNT, COURT_ANN_COUNT, TAX_RANK_COUNT, JUDGE_PROCESS_COUNT, JUDGEMENT_COUNT,

    OWING_TAX_COUNT, PENALTY_COUNT, DISHONEST_COUNT, EXECUTION_COUNT, BAIDUNEWS_COUNTMAP, GRAPH_CLUSTERS, GROUP_CONNECT_REVERSE,

    GRAPH_COMPANY, CUSTOMS_INFORMATION, ENVIRONMENT_PROTECTION,

    GRAPH_CONCERT, GRAPH_CONTRIBUTOR, GRAPH_KEY_PERSON,EXCHANGE_RATE, OVERVIEW_RELATION,GRAPH_GROUPS;

    public String get(String key) {
        return this.name().toLowerCase() + ":" + key;
    }

    public String get() {
        return this.name().toLowerCase();
    }
}
