package com.haizhi.iap.mobile.conf;

/**
 * Created by thomas on 18/4/13.
 *
 * mongo表和字段相关的信息
 */
public class MongoSchemaConstants
{
    //================================表======================================

    /**
     * 工商信息
     */
    public static final String TABLE_ENTERPRISE_DATA_GOV = "enterprise_data_gov";
    /**
     * 年报
     */
    public static final String TABLE_ANNUAL_REPORT = "annual_reports";
    /**
     * 失信信息
     */
    public static final String TABLE_SHIXING_INFO = "shixin_info";
    /**
     * 被执行信息
     */
    public static final String TABLE_ZHIXING_INFO = "zhixing_info";


    //================================字段======================================            
    /**
     * 公司名
     */
    public static final String FIELD_COMPANY = "company";
    /**
     * 记录是否被标志为删除，"逻辑上被删除"
     */
    public static final String FIELD_LOGIC_DELETE = "logic_delete";

    /**
     * 失信信息表中的公司名字段
     */
    public static final String FIELD_BUSINESS_ENTITY = "business_entity";

    /**
     * 被执行信息表中的公司名字段
     */
    public static final String FIELD_I_NAME = "i_name";
    /**
     * 股票代码
     */
    public static final String FIELD_STOCK_CODE = "stock_code";
}
