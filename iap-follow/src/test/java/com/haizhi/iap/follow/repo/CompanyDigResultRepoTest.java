package com.haizhi.iap.follow.repo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;


@ContextConfiguration(locations = {"classpath:spring/applicationContext.xml", "classpath:spring/applicationContext-data.xml"})
@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class CompanyDigResultRepoTest {

    @Autowired
    private CompanyDigResultRepo companyDigResultRepo;

    @Test
    public void findCompanyRelations() {
        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");
        list.add("3");
        list.add("4");
        List<Map> companyRelations = companyDigResultRepo.findCompanyRelations(list,new String[]{"1","2"});
        companyRelations.size();
        List<Map> companyInfos = companyDigResultRepo.findCompanyInfos(list);
        companyInfos.size();
    }

    @Test
    public void findCompanyInfos() {
        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");
        list.add("3");
        list.add("4");
        List<Map> companyInfos = companyDigResultRepo.findCompanyInfos(list);
        companyInfos.size();
    }
}