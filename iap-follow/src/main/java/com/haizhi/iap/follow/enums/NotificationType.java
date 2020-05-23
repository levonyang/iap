package com.haizhi.iap.follow.enums;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * 消息类型
 * Created by chenbo on 17/5/3.
 */
public enum NotificationType {
	
	
    MARKETING_NEW_LISTED_SHAREHOLDER(101, "new_listed_shareholder", "新增上市企业股东"),

    MARKETING_TAX_PLAYER_LELVEL_A(102, "102", "纳税等级"),

    MARKETING_BID_INFO(103, "bid_info", "招标"),

    MARKETING_WIN_BID(104, "104", "中标公告"),

    //MARKETING_NEW_AFFILIATE(105, "new_affiliate", "新设立分支机构"),
    
    MARKETING_NEW_AFFILIATE(105, "105", "新设立分支机构"),
    
    MARKETING_NEW_INVESTED_COMPANY(106, "new_invested_company", "新增对外投资"),

    //add
    ENTERPRISE_REGISTERED_CAPITAL_ADD(108,"108","工商事件"),
    
    ENTERPRISE_FRONT_SENTIMENT(109,"109","舆情信息"),

    ENTERPRISE_LEGALPERSON_CHANGE(110,"110","工商事件"),

    ENTERPRISE_COMPANYNAME_CHANGE(111,"111","工商事件"),

    ENTERPRISE_BUSSINESSSCOPE_CHANGE(112,"112","工商事件"),

    ENTERPRISE_HAVEPATENT(113,"113","专利信息"),

    ENTERPRISE_MARKET(114,"114","股市事件"),
    
    ENTERPRISE_HAVEBID(115,"115","招标事件"),
    
    ASSOCIATED_COMPANY(116,"116","舆情信息"),
    ASSOCIATED_COMPANY_MARKET_ADD(117,"117","股市事件"),
    ENTERPRISE_GAIN_EXTERNAL_INVESTMENT(118,"118","工商事件"),
    ENTERPRISE_TO_EXTERNAL_INVESTMENT(119,"119","投资事件"),    
    SENIORE_EXECUTIVE_ADD(120,"120","股市事件"),
    SENIORE_EXECUTIVE_REDUCE(121,"121","股市事件"),
    FINANCING_PRODUCT_REMIND(122,"122","产品事件"),
    
    FIXED_DEPOSIT_EXPIRES(130,"130","产品事件"),
    CORPORATE_BONDS_EXPIRE(131,"131","产品事件"),
    LARGE_DEPOSIT_CERTIFICATES_EXPIRE(132,"132","产品事件"),
    INCREASED_CORPORATE_INCOME(133,"133","财务事件"),
    INCREASED_SALES_GROWTH_OF_ENTERPRISES(134,"134","财务事件"),
    LARGE_INCREASE_IN_CORPORATE_INCOME(135,"135","财务事件"),
    QUARTERLY_INCREASE_IN_NET_PROFIT(136,"136","财务事件"),
    BIG_MONEY_TRANSFER_OUT(137,"137","账户事件"),
    LARGE_AMOUNT_OF_FUNDS_TRANSFERRED(138,"138","账户事件"),
    EQUITY_PLEDGE_EXPIRES(139,"139","产品事件"),


    //============营销与风险分隔线===========//
    RISK_COURT_ANNO(201, "court_ktgg", "开庭公告"),

    RISK_BULLETIN(202, "bulletin", "法院公告"),

    RISK_JUDGE_PROCESS(203, "judge_process", "审判流程"),

    RISK_JUDGEMENT_WENSHU(204, "judgement_wenshu", "判决结果"),

    RISK_SHIXIN_INFO(205, "shixin_info", "失信被执行"),

    RISK_OWING_TAX(206, "owing_tax", "欠税公告"),

    RISK_PENALTY(207, "penalty", "行政处罚"),

    RISK_BUSSINESS_STATUS_CHANGE(208, "business_status_change", "经营状态变更"),

    RISK_LEGAL_MAN_CHANGE(209, "legal_man_change", "法定代表人频繁变更"),

    RISK_SENIOR_EXECUTIVE_CHANGE(210, "senior_executive_change", "高管频繁变更"),

    RISK_REGISTOR_CAPITAL_CHANGE(211, "registor_capital_change", "企业注册资本频繁变更"),

    RISK_SHAREHOLDER_CHANGE(212, "shareholder_change", "股东频繁变更"),

    RISK_PLACE_CHANGE(213, "place_change", "经营地址频繁变更"),

    //code 重复，先屏蔽 begin
    //RISK_201(201,"201","公司事件"),
    //RISK_202(202,"202","管理事件"),
    //RISK_203(203,"203","违规事件"),
    //RISK_204(204,"204","账户事件"),
    //RISK_205(205,"205","担保事件"),
    //code 重复，先屏蔽 end

    RISK_214(214,"214","土地价格波动"),
    RISK_WAGE_PAYMENT_EXCEPTION(215,"215","账户事件"),
    RISK_ASSOCIATED_RISK_OVERRUN(216,"216","公司事件"),
    RISK_INSUFFICIENT_MORTGAGE_PROPERTY(217,"217","账户事件"),
    RISK_INSUFFICIENT_MORTGAGE_BONDS(218,"218","账户事件"),


    CLOSTLY_MSG_COUNTOVERVIEW(-1,"closely_msg_countoverview","关联消息数量");

    Integer code;

    String enName;

    String cnName;

    NotificationType(Integer code, String enName, String cnName) {
        this.code = code;
        this.enName = enName;
        this.cnName = cnName;
    }

    public Integer getCode() {
        return code;
    }

    public String getEnName() {
        return enName;
    }

    public String getCnName() {
        return cnName;
    }

    private static Map<Integer, NotificationType> codeAndInstanceMap = Maps.newHashMap();
    public static NotificationType get(Integer code){
        if (codeAndInstanceMap == null || codeAndInstanceMap.keySet().size() < 1){
            for (NotificationType notificationType : NotificationType.values()){
                codeAndInstanceMap.put(notificationType.getCode(), notificationType);
            }
        }
    return codeAndInstanceMap.get(code);
    }

    private static Map<String, NotificationType> enAndInstanceMap = Maps.newHashMap();
    public static NotificationType get(String enName){
        if (enAndInstanceMap == null || enAndInstanceMap.keySet().size() < 1){
            for (NotificationType notificationType : NotificationType.values()){
                enAndInstanceMap.put(notificationType.getEnName(), notificationType);
            }
        }
        return enAndInstanceMap.get(enName);
    }
    
}
