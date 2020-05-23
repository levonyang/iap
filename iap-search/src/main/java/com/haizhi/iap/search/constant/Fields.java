package com.haizhi.iap.search.constant;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Fields {
    public static final String RMB = "人民币";

	public static Map<String, String> INDEX_FIELD_TO_WIND = new HashMap<String,String>() {
		{
			put("basic_earnings_per_share","");
			put("net_assets_per_share","");
			put("net_assets_weighted_average","M1");
			put("after_deducting_earnings_per_share","");        
			put("quick_ratio","M18");
			put("current_ratio","M19");
			put("accounts_receivable_turnover_share","M21");
			put("asset_liability_ratio","M17");
			put("net_profit_margin_rate","M4");
			put("return_total_assets_rate","M3");
			put("inventory_turnover","M20");
			put("fixed_asset_turnover_rate","M23");
			put("total_asset_turnover","M24");
			put("net_worth_ratio","");
			put("fixed_assets_ratio","");
		}
	};
	
	public static Map<String, String> INDEX_FIELD_TO_COMPNAY = new HashMap<String,String>() {
		{
			put("basic_earnings_per_share","");
			put("net_assets_per_share","");
			put("net_assets_weighted_average","C01060000");
			put("after_deducting_earnings_per_share","");        
			put("quick_ratio","CW0000001");
			put("current_ratio","CW0000002");
			put("accounts_receivable_turnover_share","CW0000004");
			put("asset_liability_ratio","C09800000");
			put("net_profit_margin_rate","C01040000");
			put("return_total_assets_rate","C01100000");
			put("inventory_turnover","C09051501");
			put("fixed_asset_turnover_rate","C02020000");
			put("total_asset_turnover","C02010000");
			put("net_worth_ratio","");
			put("fixed_assets_ratio","");
		}
	};
	
	public static Map<String, String> CAIBAO_TYPE_TO_MONTH = new HashMap<String,String>() {
		{
			put("一季","03");
			put("中期","06");
			put("下季","09");
			put("年度","12");        
		}
	};
	
	public static Map<String, String> CAIBAO_TYPE_TO_FIN_INDEX_FIELD = new HashMap<String,String>() {
		{
			put("一季","Q1_TERM_BEGIN_INDEX_VAL");
			put("中期","Q2_TERM_BEGIN_INDEX_VAL");
			put("下季","Q3_TERM_BEGIN_INDEX_VAL");
			put("年度","Q4_TERM_BEGIN_INDEX_VAL");        
		}
	};

	//企业经营状态
	public static Set<String> ABNORMAL_STATUS = new HashSet<String>() {{
		add("吊销，未注销");
		add("吊销");
		add("注销");
		add("清算");
		add("停业");
		add("撤销");
	}};
}
