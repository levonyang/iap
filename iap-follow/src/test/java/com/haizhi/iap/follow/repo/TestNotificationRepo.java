package com.haizhi.iap.follow.repo;

import com.haizhi.iap.follow.enums.DataType;
import com.haizhi.iap.follow.model.MonitorCard;
import com.haizhi.iap.follow.model.ReqEditMsgs;
import com.haizhi.iap.follow.model.ReqGetMsgs;
import com.haizhi.iap.follow.utils.DateUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by haizhi on 2017/10/21.

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:spring/applicationContext.xml",
        "classpath:spring/applicationContext-data.xml",
        "file:src/main/webapp/WEB-INF/SpringMVC-servlet.xml"
})
 */
public class TestNotificationRepo {

    @Autowired
    private NotificationRepo notificationRepo;

    @Test
    public void testAddConditionsByMonth() {
        StringBuilder sql = new StringBuilder();
        List<Object> args = new ArrayList<>();
        ReqGetMsgs reqGetMsgs = new ReqGetMsgs();
        reqGetMsgs.setDataType(DataType.MONTH);

        testAddConditions(sql,args,reqGetMsgs);

        Assert.assertEquals(String.format(" and push_time > '%s'", DateUtils.getOneMonthBefore()),sql.toString());
    }

    @Test
    public void testAddConditionsByMasterCompany() {
        StringBuilder sql = new StringBuilder();
        List<Object> args = new ArrayList<>();
        ReqGetMsgs reqGetMsgs = new ReqGetMsgs();
        reqGetMsgs.setMasterCompany("小米科技");
        reqGetMsgs.setCompany("美的科技");
        reqGetMsgs.setIsClosely(true);
        reqGetMsgs.setRead(true);
        reqGetMsgs.setCount(10);
        reqGetMsgs.setOffset(15);
        reqGetMsgs.setType("RISK");
        List<Integer> l = new ArrayList<>();
        l.add(102);
        l.add(103);
        reqGetMsgs.setSubType(l);

        testAddConditions(sql,args,reqGetMsgs);

        Assert.assertEquals(String.format(" and push_time > '%s'", DateUtils.getOneMonthBefore())+
                        " and master_company = ?  and company like ?  and type > 200  and type in (?,?)  and `read` = ?  and closely = ? ",
                sql.toString());
    }

    @Test
    public void testBuildSQLByMonitorCount() {
        List<MonitorCard> monitorCardList = new ArrayList<>();
        List<Object> args = new ArrayList<>();
        Long userId = 1L;
        String dateType = DataType.MONTH;

        MonitorCard m0 = new MonitorCard();
        m0.setCompany("小米科技");

        MonitorCard m1 = new MonitorCard();
        m1.setCompany("美的科技");

        MonitorCard m2 = new MonitorCard();
        m2.setCompany("海致星图科技");

        monitorCardList.add(m0);
        monitorCardList.add(m1);
        monitorCardList.add(m2);

        String resp = testBuildSQLByMonitorCount(monitorCardList,args,userId,dateType);

        String re = " select ? company, (select count(1) from notification where user_id =? and company = ? and closely = false and type > 200 "
                +String.format(" and push_time > '%s'", DateUtils.getOneMonthBefore())+" ) as risk ,(select count(1) from notification where user_id =? and company = ? and closely = false and type > 0 and type < 200 "
                +String.format(" and push_time > '%s'", DateUtils.getOneMonthBefore())+" ) as marketing , (select count(1) from notification where user_id =? and master_company = ? and closely = true and type > 200 "
                +String.format(" and push_time > '%s'", DateUtils.getOneMonthBefore())+" ) as closelyRisk , (select count(1) from notification where user_id =? and master_company = ? and closely = true and type > 0 and type < 200 "
                +String.format(" and push_time > '%s'", DateUtils.getOneMonthBefore())+" ) as closelyMarketing  union all select ? company, (select count(1) from notification where user_id =? and company = ? and closely = false and type > 200 "
                +String.format(" and push_time > '%s'", DateUtils.getOneMonthBefore())+" ) as risk ,(select count(1) from notification where user_id =? and company = ? and closely = false and type > 0 and type < 200 "
                +String.format(" and push_time > '%s'", DateUtils.getOneMonthBefore())+" ) as marketing , (select count(1) from notification where user_id =? and master_company = ? and closely = true and type > 200 "
                +String.format(" and push_time > '%s'", DateUtils.getOneMonthBefore())+" ) as closelyRisk , (select count(1) from notification where user_id =? and master_company = ? and closely = true and type > 0 and type < 200 "
                +String.format(" and push_time > '%s'", DateUtils.getOneMonthBefore())+" ) as closelyMarketing  union all select ? company, (select count(1) from notification where user_id =? and company = ? and closely = false and type > 200 "
                +String.format(" and push_time > '%s'", DateUtils.getOneMonthBefore())+" ) as risk ,(select count(1) from notification where user_id =? and company = ? and closely = false and type > 0 and type < 200 "
                +String.format(" and push_time > '%s'", DateUtils.getOneMonthBefore())+" ) as marketing , (select count(1) from notification where user_id =? and master_company = ? and closely = true and type > 200 "
                +String.format(" and push_time > '%s'", DateUtils.getOneMonthBefore())+" ) as closelyRisk , (select count(1) from notification where user_id =? and master_company = ? and closely = true and type > 0 and type < 200 "
                +String.format(" and push_time > '%s'", DateUtils.getOneMonthBefore())+" ) as closelyMarketing ";
        Assert.assertEquals(re,resp);
    }

    @Test
    public void testAddConditionByCollectedAndClosely() {
        StringBuilder sql = new StringBuilder();
        List<Object> args = new ArrayList<>();
        ReqEditMsgs reqEditMsgs = new ReqEditMsgs();
        reqEditMsgs.setCollected(true);
        reqEditMsgs.setIsAllEdit(true);
        reqEditMsgs.setRead(true);
        reqEditMsgs.setIsClosely(true);

        testAddCondition(sql,args,reqEditMsgs);

        Assert.assertEquals(" and collected = ? and closely = ?",sql.toString());
    }

    @Test
    public void testAddConditionByType() {
        StringBuilder sql = new StringBuilder();
        List<Object> args = new ArrayList<>();
        ReqEditMsgs reqEditMsgs = new ReqEditMsgs();
        reqEditMsgs.setType("risk");

        testAddCondition(sql,args,reqEditMsgs);

        Assert.assertEquals(" and type > 200",sql.toString());
    }

    @Test
    public void testAddConditionBySubType() {
        StringBuilder sql = new StringBuilder();
        List<Object> args = new ArrayList<>();
        ReqEditMsgs reqEditMsgs = new ReqEditMsgs();

        List<Integer> l = new ArrayList<>();
        l.add(102);
        l.add(104);
        reqEditMsgs.setSubType(l);

        testAddCondition(sql,args,reqEditMsgs);

        Assert.assertEquals(" and type in (102,104)",sql.toString());
    }

    @Test
    public void testAddConditionByAll() {
        StringBuilder sql = new StringBuilder();
        List<Object> args = new ArrayList<>();
        ReqEditMsgs reqEditMsgs = new ReqEditMsgs();
        reqEditMsgs.setIsClosely(true);
        reqEditMsgs.setCollected(true);
        reqEditMsgs.setType("marketing");
        List<Integer> l = new ArrayList<>();
        l.add(102);
        l.add(104);
        reqEditMsgs.setSubType(l);

        testAddCondition(sql,args,reqEditMsgs);

        Assert.assertEquals(" and collected = ? and closely = ? and type < 200 and type > 0 and type in (102,104)",
                sql.toString());
    }

    private void testAddCondition(StringBuilder sql,List<Object> args,ReqEditMsgs reqEditMsgs) {
        Method method = null;
        try {
            method = AbstractNotificationRepo.class.getDeclaredMethod("addCondition", new Class[]{
                    StringBuilder.class, List.class, ReqEditMsgs.class
            });
            method.setAccessible(true);
            method.invoke(NotificationRepo.class.newInstance(),sql,args,reqEditMsgs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void testAddConditions(StringBuilder sql,List<Object> args,ReqGetMsgs reqGetMsgs) {
        Method method = null;
        try {
            method = AbstractNotificationRepo.class.getDeclaredMethod("addConditions", new Class[]{
                    StringBuilder.class, List.class, ReqGetMsgs.class
            });
            method.setAccessible(true);
            method.invoke(NotificationRepo.class.newInstance(),sql,args,reqGetMsgs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String testBuildSQLByMonitorCount(List<MonitorCard> monitorCardList, List<Object> args,Long userId, String dateType) {
        Method method = null;
        try {
            method = AbstractNotificationRepo.class.getDeclaredMethod("buildSQLByMonitorCount", new Class[]{
                    List.class, List.class, Long.class,String.class
            });
            method.setAccessible(true);
            return method.invoke(NotificationRepo.class.newInstance(),monitorCardList,args,userId,dateType).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setNotificationRepo(NotificationRepo notificationRepo) {
        this.notificationRepo = notificationRepo;
    }
}
