package com.haizhi.iap.mobile.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by thomas on 18/4/19.
 *
 * 客户所属关系表
 * @see com.haizhi.iap.mobile.conf.SqlSchemaConstants#TABLE_CUSTOMER_BELONG
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerBelong
{
    private String customerId;
    private String customerName;
    private String manager;
    private String relationType;
    private String loadDate;
}
