package com.haizhi.iap.search.conf;

import com.haizhi.iap.common.utils.ConfUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Created by chenbo on 17/3/21.
 */
@Slf4j
public class ListingConf {
    private static Map<String, String> ssgsBaseInfoMap;
    private static Map<String, String> caiBaoMap;

    public static Map<String, String> getSsgsBaseInfoMap() {
        if (ssgsBaseInfoMap == null) {
            ssgsBaseInfoMap = ConfUtil.getConfMap("ssgs_baseinfo.conf");
        }
        return ssgsBaseInfoMap;
    }

    public static Map<String, String> getCaiBaoMap(){
        if(caiBaoMap == null){
            caiBaoMap = ConfUtil.getConfMap("ssgs_caibao.conf");
        }
        return caiBaoMap;
    }
}
/**
 * 法定代表人,legal_man
 * 转配股,transferred_allotted_shares
 * H股,h_share
 * 发行市盈率,issuing_ratio
 * 股票代码全称,code
 * 邮政编码,postal_code
 * 上市推荐人,listed_references
 * 英文名称,english_name
 * 上市版块,public_sector
 * 招股时间,offer_time
 * 国家股,country_share
 * 公司电话,company_phone
 * 注册资本,registered_capital
 * 公司全称,company_full_name
 * 上市时间,market_time
 * 发行价格,issue_price
 * 保荐机构,sponsor_company
 * 每股净资产,net_assets_per_share
 * 法人股,legal_person_share
 * 公司简称,company_referred
 * 主承销商,lead_underwriter
 * 发起人股,promoter_shares
 * 每股未分配利润,undistributed_profit_per_share
 * 公司董秘,company_chairman_secretary
 * 详情链接,detail_link
 * 公司网址,company_website
 * 公司传真,company_fax
 * 发行方式,distribution_type
 * 股票简称,shares_referred
 * 股票代码,stock_code
 * 每股资本公积金,share_capital_reserve_fund
 * 总股本,total_share
 * 行业种类,industry
 * 每股收益,earnings_per_share
 * 流通股,outstanding_share
 * B股,b_share
 * 发行数量,issue_number
 * 净资产收益率,net_income_rate
 * 注册地址,registered_address
 * 高管人员,executives
 */
