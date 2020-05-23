package com.haizhi.iap.configure.repo;


import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/spring/*.xml"})
public class GraphRelationRepoTest {

    @Autowired
    private GraphRelationRepo graphRelationRepo;

    @Test
    public void buildFilterConditionTest1() {
        String ret = graphRelationRepo.buildFilterCondition("key1", "value1", "==");
        Assert.assertEquals(" AND d.key1 == 'value1'", ret);
        ret = graphRelationRepo.buildFilterCondition("key1", "2017-10-01", ">=");
        Assert.assertEquals(" AND d.key1 >= '2017-10-01'", ret);
        ret = graphRelationRepo.buildFilterCondition("key1", "2017-10-01", "<=");
        Assert.assertEquals(" AND d.key1 <= '2017-10-01'", ret);
    }

    @Test
    public void buildFilterConditionTest2() {
        String ret = graphRelationRepo.buildFilterCondition("key1", 100, "==");
        Assert.assertEquals(" AND d.key1 == 100", ret);
        ret = graphRelationRepo.buildFilterCondition("key1", 100, ">=");
        Assert.assertEquals(" AND d.key1 >= 100", ret);
        ret = graphRelationRepo.buildFilterCondition("key1", 100, "<=");
        Assert.assertEquals(" AND d.key1 <= 100", ret);
    }

}
