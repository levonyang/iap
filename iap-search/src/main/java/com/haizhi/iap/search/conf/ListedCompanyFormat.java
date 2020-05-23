package com.haizhi.iap.search.conf;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by chenbo on 17/2/20.
 */
public class ListedCompanyFormat {
    private static final String DEFAULT_VALUE = "暂无";

    private static FinancialReportItem companyAbility = new FinancialReportItem();
    private static FinancialReportItem cashFlow = new FinancialReportItem();
    private static FinancialReportItem assetsLiability = new FinancialReportItem();
    private static FinancialReportItem profit = new FinancialReportItem();


    static {
        //公司综合能力指标
        FRItemData investAndEarning = new FRItemData();
        FRItemData repayAbility = new FRItemData();
        FRItemData profitAbility = new FRItemData();
        FRItemData manageAbility = new FRItemData();
        FRItemData compositionOfCapital = new FRItemData();

        List<FRItemDataListData> investAndEarningList = Lists.newArrayList();
        List<FRItemDataListData> repayAbilityList = Lists.newArrayList();
        List<FRItemDataListData> profitAbilityList = Lists.newArrayList();
        List<FRItemDataListData> manageAbilityList = Lists.newArrayList();
        List<FRItemDataListData> compositionOfCapitalList = Lists.newArrayList();

        investAndEarningList.add(new FRItemDataListData("basic_earnings_per_share", "基本每股收益(元)", DEFAULT_VALUE));
        investAndEarningList.add(new FRItemDataListData("net_assets_per_share", "每股净资产(元)", DEFAULT_VALUE));
        investAndEarningList.add(new FRItemDataListData("net_assets_weighted_average", "净资产收益率—加权平均(%)", DEFAULT_VALUE));
        investAndEarningList.add(new FRItemDataListData("after_deducting_earnings_per_share", "扣除后每股收益(元)", DEFAULT_VALUE));

        repayAbilityList.add(new FRItemDataListData("current_ratio", "流动比率(倍)", DEFAULT_VALUE));
        repayAbilityList.add(new FRItemDataListData("quick_ratio", "速动比率 (倍)", DEFAULT_VALUE));
        repayAbilityList.add(new FRItemDataListData("accounts_receivable_turnover_share", "应收帐款周转率(次)", DEFAULT_VALUE));
        repayAbilityList.add(new FRItemDataListData("asset_liability_ratio", "资产负债比率(%)", DEFAULT_VALUE));

        profitAbilityList.add(new FRItemDataListData("net_profit_margin_rate", "净利润率(%)", DEFAULT_VALUE));
        profitAbilityList.add(new FRItemDataListData("return_total_assets_rate", "总资产报酬率(%)", DEFAULT_VALUE));

        manageAbilityList.add(new FRItemDataListData("inventory_turnover", "存货周转率", DEFAULT_VALUE));
        manageAbilityList.add(new FRItemDataListData("fixed_asset_turnover_rate", "固定资产周转率", DEFAULT_VALUE));
        manageAbilityList.add(new FRItemDataListData("total_asset_turnover", "总资产周转率", DEFAULT_VALUE));

        compositionOfCapitalList.add(new FRItemDataListData("net_worth_ratio", "净资产比率(%)", DEFAULT_VALUE));
        compositionOfCapitalList.add(new FRItemDataListData("fixed_assets_ratio", "固定资产比率(%)", DEFAULT_VALUE));

        investAndEarning.setTitle("一.投资与收益");
        investAndEarning.setList(investAndEarningList);
        repayAbility.setTitle("二.偿债能力");
        repayAbility.setList(repayAbilityList);
        profitAbility.setTitle("三.盈利能力");
        profitAbility.setList(profitAbilityList);
        manageAbility.setTitle("四.经营能力");
        manageAbility.setList(manageAbilityList);
        compositionOfCapital.setTitle("五.资本构成");
        compositionOfCapital.setList(compositionOfCapitalList);

        List<FRItemData> dataList = Lists.newArrayList();
        dataList.add(investAndEarning);
        dataList.add(repayAbility);
        dataList.add(profitAbility);
        dataList.add(manageAbility);
        dataList.add(compositionOfCapital);

        companyAbility.setItem("综合能力指标");
        companyAbility.setData(dataList);

        //现金流量表
        FRItemData manage = new FRItemData();
        FRItemData invest = new FRItemData();
        FRItemData raise = new FRItemData();
        FRItemData changeReason = new FRItemData();

        List<FRItemDataListData> manageList = Lists.newArrayList();
        List<FRItemDataListData> investList = Lists.newArrayList();
        List<FRItemDataListData> raiseList = Lists.newArrayList();
        List<FRItemDataListData> changeReasonList = Lists.newArrayList();

        manageList.add(new FRItemDataListData("cash_sell_goods_providing_services", "销售商品、提供劳务所收到的现金", DEFAULT_VALUE));
        manageList.add(new FRItemDataListData("received_refund", "收到的税费返还", DEFAULT_VALUE));
        manageList.add(new FRItemDataListData("Other_business_activities_cash", "收到其他与经营活动有关的现金", DEFAULT_VALUE));
        manageList.add(new FRItemDataListData("purchase_goods_accepting_services_cash", "购买商品、接受劳务支付的现金", DEFAULT_VALUE));
        manageList.add(new FRItemDataListData("paid_worker_cash", "支付给职工以及为职工支付的现金", DEFAULT_VALUE));
        manageList.add(new FRItemDataListData("pay_various_taxes", "支付的各项税费", DEFAULT_VALUE));
        manageList.add(new FRItemDataListData("other_business_activities_cashpayment", "支付其他与经营活动有关的现金", DEFAULT_VALUE));
        manageList.add(new FRItemDataListData("operating_activities_outflows_subtotal", "经营活动现金流出小计", DEFAULT_VALUE));
        manageList.add(new FRItemDataListData("net_business_generated_cash_flow", "经营活动产生的现金流量净额", DEFAULT_VALUE));

        investList.add(new FRItemDataListData("get_money_received_cash", "收回投资收到的现金", DEFAULT_VALUE));
        investList.add(new FRItemDataListData("get_cash_nvestment_income", "取得投资收益收到的现金", DEFAULT_VALUE));
        investList.add(new FRItemDataListData("Other_business_activities_cash", "收到其他与经营活动有关的现金", DEFAULT_VALUE));
        investList.add(new FRItemDataListData("disposal_fixed_assets_intangible_assets", "处置固定资产、无形资产和其他长期资产收回的现金净额", DEFAULT_VALUE));
        investList.add(new FRItemDataListData("disposal_subsidiaries_business_received_net_cash", "处置子公司及其他营业单位收到的现金净额", DEFAULT_VALUE));
        investList.add(new FRItemDataListData("other_investment_cash_received", "收到其他与投资活动有关的现金", DEFAULT_VALUE));
        investList.add(new FRItemDataListData("construction_assets_intangible_assets_cash", "购建固定资产、无形资产和其他长期资产支付的现金", DEFAULT_VALUE));
        investList.add(new FRItemDataListData("investment_paid_cash", "投资支付的现金", DEFAULT_VALUE));
        investList.add(new FRItemDataListData("acquire_subsidiary_other_business_paycash", "取得子公司及其他营业单位支付的现金净额", DEFAULT_VALUE));
        investList.add(new FRItemDataListData("other_investment_payment", "支付其他与投资活动有关的现金", DEFAULT_VALUE));
        investList.add(new FRItemDataListData("absorb_investment_cash", "吸收投资收到的现金", DEFAULT_VALUE));
        investList.add(new FRItemDataListData("borrow_money_received_cash", "取得借款收到的现金", DEFAULT_VALUE));
        investList.add(new FRItemDataListData("investment_cash_inflows_subtotal", "投资活动现金流入小计", DEFAULT_VALUE));
        investList.add(new FRItemDataListData("investment_cash_outflows_subtotal", "投资活动现金流出小计", DEFAULT_VALUE));
        investList.add(new FRItemDataListData("net_investment_generated_cashflow", "投资活动产生的现金流量净额", DEFAULT_VALUE));

        raiseList.add(new FRItemDataListData("other_financing_cash", "收到其他与筹资活动有关的现金", DEFAULT_VALUE));
        raiseList.add(new FRItemDataListData("repayment_debt_cash", "偿还债务支付的现金", DEFAULT_VALUE));
        raiseList.add(new FRItemDataListData("distribution_dividends_profits_repay", "分配股利、利润或偿还利息支付的现金", DEFAULT_VALUE));
        raiseList.add(new FRItemDataListData("other_financing_activities_ash", "支付其他与筹资活动有关的现金", DEFAULT_VALUE));
        raiseList.add(new FRItemDataListData("financing_activities_cash_inflows_subtotal", "筹资活动现金流入小计", DEFAULT_VALUE));
        raiseList.add(new FRItemDataListData("financing_cash_outflows", "筹资活动现金流出小计", DEFAULT_VALUE));
        raiseList.add(new FRItemDataListData("net_cashflow_generated_financing", "筹资活动产生的现金流量净额", DEFAULT_VALUE));

        changeReasonList.add(new FRItemDataListData("influence_exchange_rate_cash", "汇率变动对现金的影响", DEFAULT_VALUE));
        changeReasonList.add(new FRItemDataListData("influence_other_reasons_cash", "其他原因对现金的影响", DEFAULT_VALUE));
        changeReasonList.add(new FRItemDataListData("net_increase_cash", "现金及现金等价物净增加额", DEFAULT_VALUE));
        changeReasonList.add(new FRItemDataListData("beginning_balance_cash_equivalents", "期初现金及现金等价物余额", DEFAULT_VALUE));
        changeReasonList.add(new FRItemDataListData("final_balance_cash_equivalents", "期末现金及现金等价物余额", DEFAULT_VALUE));

        manage.setTitle("一.经营活动产生的现金流量'");
        manage.setList(manageList);
        invest.setTitle("二.投资活动产生的现金流量");
        invest.setList(investList);
        raise.setTitle("三.筹资活动产生的现金流量");
        raise.setList(raiseList);
        changeReason.setTitle("四.现金变动原因");
        changeReason.setList(changeReasonList);

        List<FRItemData> cashList = Lists.newArrayList();
        cashList.add(manage);
        cashList.add(invest);
        cashList.add(raise);
        cashList.add(changeReason);
        cashFlow.setItem("现金流量表");
        cashFlow.setData(cashList);

        //资产负债表
        FRItemData currentAssets = new FRItemData();
        FRItemData fixedAssets = new FRItemData();
        FRItemData currentLiability = new FRItemData();
        FRItemData fixedLiability = new FRItemData();
        FRItemData shEquity = new FRItemData();

        List<FRItemDataListData> currentAssetsList = Lists.newArrayList();
        List<FRItemDataListData> fixedAssetsList = Lists.newArrayList();
        List<FRItemDataListData> currentLiabilityList = Lists.newArrayList();
        List<FRItemDataListData> fixedLiabilityList = Lists.newArrayList();
        List<FRItemDataListData> shEquityList = Lists.newArrayList();

        currentAssetsList.add(new FRItemDataListData("monetary_fund", "货币资金", DEFAULT_VALUE));
        currentAssetsList.add(new FRItemDataListData("transactional_financial_assets", "交易性金融资产", DEFAULT_VALUE));
        currentAssetsList.add(new FRItemDataListData("notes_receivable", "应收票据", DEFAULT_VALUE));
        currentAssetsList.add(new FRItemDataListData("accounts_receivable", "应收账款", DEFAULT_VALUE));
        currentAssetsList.add(new FRItemDataListData("prepayments", "预付款项", DEFAULT_VALUE));
        currentAssetsList.add(new FRItemDataListData("other_receivables", "其他应收款", DEFAULT_VALUE));
        currentAssetsList.add(new FRItemDataListData("affiliate_accounts_receivable", "应收关联公司款", DEFAULT_VALUE));
        currentAssetsList.add(new FRItemDataListData("interest_receivable", "应收利息", DEFAULT_VALUE));
        currentAssetsList.add(new FRItemDataListData("dividends_receivable", "应收股利", DEFAULT_VALUE));
        currentAssetsList.add(new FRItemDataListData("inventory", "存货", DEFAULT_VALUE));
        currentAssetsList.add(new FRItemDataListData("consumptive_biological_assets", "消耗性生物资产", DEFAULT_VALUE));
        currentAssetsList.add(new FRItemDataListData("non_current_assets_matured_year", "一年内到期的非流动资产", DEFAULT_VALUE));
        currentAssetsList.add(new FRItemDataListData("other_liquid_assets", "其他流动资产", DEFAULT_VALUE));
        currentAssetsList.add(new FRItemDataListData("current_assets_total", "流动资产合计", DEFAULT_VALUE));

        fixedAssetsList.add(new FRItemDataListData("available_sale_financial_assets", "可供出售金融资产", DEFAULT_VALUE));
        fixedAssetsList.add(new FRItemDataListData("held_maturity_investments", "持有至到期投资", DEFAULT_VALUE));
        fixedAssetsList.add(new FRItemDataListData("long_term_receivables", "长期应收款", DEFAULT_VALUE));
        fixedAssetsList.add(new FRItemDataListData("long_term_equity_investment", "长期股权投资", DEFAULT_VALUE));
        fixedAssetsList.add(new FRItemDataListData("investment_real_estate", "投资性房地产", DEFAULT_VALUE));
        fixedAssetsList.add(new FRItemDataListData("fixed_assets", "固定资产", DEFAULT_VALUE));
        fixedAssetsList.add(new FRItemDataListData("projects_construction", "在建工程", DEFAULT_VALUE));
        fixedAssetsList.add(new FRItemDataListData("engineering_materials", "工程物资", DEFAULT_VALUE));
        fixedAssetsList.add(new FRItemDataListData("fixed_assets_clean", "固定资产清理", DEFAULT_VALUE));
        fixedAssetsList.add(new FRItemDataListData("productive_biological_assets", "生产性生物资产", DEFAULT_VALUE));
        fixedAssetsList.add(new FRItemDataListData("oil_gas_assets", "油气资产", DEFAULT_VALUE));
        fixedAssetsList.add(new FRItemDataListData("intangible_assets", "无形资产", DEFAULT_VALUE));
        fixedAssetsList.add(new FRItemDataListData("development_spending", "开发支出", DEFAULT_VALUE));
        fixedAssetsList.add(new FRItemDataListData("goodwill", "商誉", DEFAULT_VALUE));
        fixedAssetsList.add(new FRItemDataListData("long_term_prepaid_expenses", "长期待摊费用", DEFAULT_VALUE));
        fixedAssetsList.add(new FRItemDataListData("deferred_tax_assets", "递延所得税资产", DEFAULT_VALUE));
        fixedAssetsList.add(new FRItemDataListData("other_non_current_assets", "其他非流动资产", DEFAULT_VALUE));
        fixedAssetsList.add(new FRItemDataListData("total_non_current_assets", "非流动资产合计", DEFAULT_VALUE));
        fixedAssetsList.add(new FRItemDataListData("total_assets", "资产总计", DEFAULT_VALUE));

        currentLiabilityList.add(new FRItemDataListData("short_term_borrowing", "短期借款", DEFAULT_VALUE));
        currentLiabilityList.add(new FRItemDataListData("tradable_financial_liabilities", "交易性金融负债", DEFAULT_VALUE));
        currentLiabilityList.add(new FRItemDataListData("notes_payable", "应付票据", DEFAULT_VALUE));
        currentLiabilityList.add(new FRItemDataListData("accounts_ayable", "应付账款", DEFAULT_VALUE));
        currentLiabilityList.add(new FRItemDataListData("advance_payment", "预收款项", DEFAULT_VALUE));
        currentLiabilityList.add(new FRItemDataListData("employee_compensation", "应付职工薪酬", DEFAULT_VALUE));
        currentLiabilityList.add(new FRItemDataListData("payable_taxes", "应交税费", DEFAULT_VALUE));
        currentLiabilityList.add(new FRItemDataListData("interest_payable", "应付利息", DEFAULT_VALUE));
        currentLiabilityList.add(new FRItemDataListData("dividends_payable", "应付股利", DEFAULT_VALUE));
        currentLiabilityList.add(new FRItemDataListData("other_payables", "其他应付款", DEFAULT_VALUE));
        currentLiabilityList.add(new FRItemDataListData("deal_related_companies", "应付关联公司款", DEFAULT_VALUE));
        currentLiabilityList.add(new FRItemDataListData("non_current_liabilities_one_year", "一年内到期的非流动负债", DEFAULT_VALUE));
        currentLiabilityList.add(new FRItemDataListData("other_current_liabilities", "其他流动负债", DEFAULT_VALUE));
        currentLiabilityList.add(new FRItemDataListData("total_current_liabilities", "流动负债合计", DEFAULT_VALUE));

        fixedLiabilityList.add(new FRItemDataListData("long_term_borrowing", "长期借款", DEFAULT_VALUE));
        fixedLiabilityList.add(new FRItemDataListData("bonds_payable", "应付债券", DEFAULT_VALUE));
        fixedLiabilityList.add(new FRItemDataListData("long_term_accounts_payable", "长期应付款", DEFAULT_VALUE));
        fixedLiabilityList.add(new FRItemDataListData("specific_payable", "专项应付款", DEFAULT_VALUE));
        fixedLiabilityList.add(new FRItemDataListData("estimated_debts", "预计负债", DEFAULT_VALUE));
        fixedLiabilityList.add(new FRItemDataListData("deferred_income_tax_liabilities", "递延所得税负债", DEFAULT_VALUE));
        fixedLiabilityList.add(new FRItemDataListData("other_non_current_liabilities", "其他非流动负债", DEFAULT_VALUE));
        fixedLiabilityList.add(new FRItemDataListData("total_non_current_liabilities", "非流动负债合计", DEFAULT_VALUE));
        fixedLiabilityList.add(new FRItemDataListData("total_liabilities", "负债合计", DEFAULT_VALUE));

        shEquityList.add(new FRItemDataListData("paid_in_capital", "实收资本(或股本)", DEFAULT_VALUE));
        shEquityList.add(new FRItemDataListData("capital_reserves", "资本公积", DEFAULT_VALUE));
        shEquityList.add(new FRItemDataListData("surplus_reserves", "盈余公积", DEFAULT_VALUE));
        shEquityList.add(new FRItemDataListData("treasury_stock", "库存股", DEFAULT_VALUE));
        shEquityList.add(new FRItemDataListData("undistributed_profit", "未分配利润", DEFAULT_VALUE));
        shEquityList.add(new FRItemDataListData("rights_minority_shareholders", "少数股东权益", DEFAULT_VALUE));
        shEquityList.add(new FRItemDataListData("translate_foreign_statements_spreads", "外币报表折算价差", DEFAULT_VALUE));
        shEquityList.add(new FRItemDataListData("abnormal_project_income_adjustment", "非正常经营项目收益调整", DEFAULT_VALUE));
        shEquityList.add(new FRItemDataListData("owners_equity_or_shareholders_equity", "所有者权益（或股东权益）合计", DEFAULT_VALUE));
        shEquityList.add(new FRItemDataListData("liabilities_and_owners_equity_or_shareholders_equity", "负债和所有者权益（或股东权益）总计", DEFAULT_VALUE));

        currentAssets.setTitle("一.流动资产");
        currentAssets.setList(currentAssetsList);
        fixedAssets.setTitle("二.非流动资产");
        fixedAssets.setList(fixedAssetsList);
        currentLiability.setTitle("三.流动负债");
        currentLiability.setList(currentLiabilityList);
        fixedLiability.setTitle("四.非流动负债");
        fixedLiability.setList(fixedLiabilityList);
        shEquity.setTitle("五.股东权益");
        shEquity.setList(shEquityList);

        List<FRItemData> assetsList = Lists.newArrayList();
        assetsList.add(currentAssets);
        assetsList.add(fixedAssets);
        assetsList.add(currentLiability);
        assetsList.add(fixedLiability);
        assetsList.add(shEquity);

        assetsLiability.setItem("资产负债表");
        assetsLiability.setData(assetsList);

        //利润表
        FRItemData opIncome = new FRItemData();
        FRItemData opProfit = new FRItemData();
        FRItemData profitTotal = new FRItemData();
        FRItemData retainedProfit = new FRItemData();

        List<FRItemDataListData> opIncomeList = Lists.newArrayList();
        List<FRItemDataListData> opProfitList = Lists.newArrayList();
        List<FRItemDataListData> profitTotalList = Lists.newArrayList();
        List<FRItemDataListData> retainedProfitList = Lists.newArrayList();

        opIncomeList.add(new FRItemDataListData("Operating_income", "营业收入", DEFAULT_VALUE));
        opIncomeList.add(new FRItemDataListData("operating_cost", "营业成本", DEFAULT_VALUE));
        opIncomeList.add(new FRItemDataListData("business_tax_additional", "营业税金及附加", DEFAULT_VALUE));
        opIncomeList.add(new FRItemDataListData("cost_sales", "销售费用", DEFAULT_VALUE));
        opIncomeList.add(new FRItemDataListData("management_fees", "管理费用", DEFAULT_VALUE));
        opIncomeList.add(new FRItemDataListData("exploration_cost", "勘探费用", DEFAULT_VALUE));
        opIncomeList.add(new FRItemDataListData("finance_charges", "财务费用", DEFAULT_VALUE));
        opIncomeList.add(new FRItemDataListData("asset_impairment_loss", "资产减值损失", DEFAULT_VALUE));
        opIncomeList.add(new FRItemDataListData("changes_net_income", "公允价值变动净收益", DEFAULT_VALUE));
        opIncomeList.add(new FRItemDataListData("return_on_investment", "投资收益", DEFAULT_VALUE));
        opIncomeList.add(new FRItemDataListData("investments_associated_and_joint_company", "对联营企业和合营企业的投资权益", DEFAULT_VALUE));
        opIncomeList.add(new FRItemDataListData("operating_profit_other_subjects", "影响营业利润的其他科目", DEFAULT_VALUE));

        opProfitList.add(new FRItemDataListData("operating_profit", "营业利润", DEFAULT_VALUE));
        opProfitList.add(new FRItemDataListData("subsidies_income", "补贴收入", DEFAULT_VALUE));
        opProfitList.add(new FRItemDataListData("non_operating_income", "营业外收入", DEFAULT_VALUE));
        opProfitList.add(new FRItemDataListData("non_business_expenses", "营业外支出", DEFAULT_VALUE));
        opProfitList.add(new FRItemDataListData("non_current_assets_disposal_net_losses", "非流动资产处置净损失", DEFAULT_VALUE));
        opProfitList.add(new FRItemDataListData("affect_total_amount_profits_other_subjects", "影响利润总额的其他科目", DEFAULT_VALUE));

        profitTotalList.add(new FRItemDataListData("profit_total", "利润总额", DEFAULT_VALUE));
        profitTotalList.add(new FRItemDataListData("income_tax", "所得税", DEFAULT_VALUE));
        profitTotalList.add(new FRItemDataListData("affect_net_profit_other_subjects", "影响净利润的其他科目", DEFAULT_VALUE));

        retainedProfitList.add(new FRItemDataListData("net_profit_attributable_parent_company", "归属于母公司所有者的净利润", DEFAULT_VALUE));
        retainedProfitList.add(new FRItemDataListData("minority_shareholders_profit_loss", "少数股东损益", DEFAULT_VALUE));

        opIncome.setTitle("一.营业收入");
        opIncome.setList(opIncomeList);
        opProfit.setTitle("二.营业利润");
        opProfit.setList(opProfitList);
        profitTotal.setTitle("三.利润总额");
        profitTotal.setList(profitTotalList);
        retainedProfit.setTitle("四.净利润");
        retainedProfit.setList(retainedProfitList);

        List<FRItemData> profitList = Lists.newArrayList();
        profitList.add(opIncome);
        profitList.add(opProfit);
        profitList.add(profitTotal);
        profitList.add(retainedProfit);

        profit.setItem("利润表");
        profit.setData(profitList);

    }

    public static FinancialReportItem getCompanyAbility() {
        return companyAbility;
    }

    public static FinancialReportItem getCashFlow() {
        return cashFlow;
    }

    public static FinancialReportItem getAssetsLiability() {
        return assetsLiability;
    }

    public static FinancialReportItem getProfit() {
        return profit;
    }
}
