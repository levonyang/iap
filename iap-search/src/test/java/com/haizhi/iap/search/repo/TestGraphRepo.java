package com.haizhi.iap.search.repo;

import com.alibaba.fastjson.JSON;
import com.haizhi.iap.search.controller.model.PageArangoParam;
import com.haizhi.iap.search.controller.model.ReqGuaranteeOrTransfer;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by haizhi on 2017/11/23.
 */
public class TestGraphRepo {

    @Test
    public void testGetPageArangoParam() {
        String req = "{\n" +
                "\t\"type\":\"test_danbao_1509965359588\",\n" +
                "\t\"offset\":0,\n" +
                "\t\"count\":5,\n" +
                "\t\"conditionList\":[{\n" +
                "\t\t\"begin_date\": \"2016-07-08\",\n" +
                "\t    \"currency\": \"人民币\",\n" +
                "\t    \"danbao_type\": \"一般担保\",\n" +
                "\t    \"end_date\": \"2017-07-08\",\n" +
                "\t    \"value\": 5000000\n" +
                "\t}],\n" +
                "\t\"from\":\"Company/3A6199DB9D2B9F51836BCE8897F9B7C8\",\n" +
                "\t\"to\":\"Company/3963E929C0BE8277843484EAB61C04BD\"\n" +
                "}";
        ReqGuaranteeOrTransfer request = JSON.parseObject(req, ReqGuaranteeOrTransfer.class);
        GraphRepo repo = new GraphRepo();
        PageArangoParam param = repo.getPageArangoParam(request);

        Assert.assertEquals("FOR doc IN test_danbao_1509965359588 FILTER  doc.end_date == @param2 AND  doc.danbao_type == @param3 AND  doc.begin_date == @param4 AND  doc.currency == @param5 AND  doc.value == @param6 AND  doc._from == 'Company/3A6199DB9D2B9F51836BCE8897F9B7C8' AND doc._to == 'Company/3963E929C0BE8277843484EAB61C04BD' LIMIT 0,5 RETURN doc",param.getAql());
        Assert.assertEquals("RETURN LENGTH(FOR doc IN test_danbao_1509965359588 FILTER  doc.end_date == @param2 AND  doc.danbao_type == @param3 AND  doc.begin_date == @param4 AND  doc.currency == @param5 AND  doc.value == @param6 AND  doc._from == 'Company/3A6199DB9D2B9F51836BCE8897F9B7C8' AND doc._to == 'Company/3963E929C0BE8277843484EAB61C04BD'  RETURN doc)",param.getAqlCount());
        Assert.assertEquals("{param5=人民币, param6=5000000, param3=一般担保, param4=2016-07-08, param2=2017-07-08}",param.getBindVars().toString());
    }

    @Test
    public void testCastDouble() {
        String str = "aaaaa";
        Object res = null;
        try {
            res = Double.valueOf(str);
        } catch (Exception e) {
            res = str;
        }
        Assert.assertEquals(str,res);

        String param = "500.00";
        try {
            res = Double.valueOf(param);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertEquals("500.0",res.toString());
    }
}
