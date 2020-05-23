package com.haizhi.iap.search.service;

import com.haizhi.iap.search.controller.model.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.List;

/**
 * Created by chenbo on 2017/10/16.
 */
//@Slf4j
public class EnterpriseServiceTest {

   /* ClassPathXmlApplicationContext context;
    EnterpriseSearchService enterpriseSearchService;
    EnterpriseReq req;

    @Before
    public void init() {
        context = new ClassPathXmlApplicationContext("classpath:spring/applicationContext.xml",
                "classpath:spring/applicationContext-data.xml");
        enterpriseSearchService = context.getBean(EnterpriseSearchService.class);
    }

    @Test
    public void search() {
        String company = "招商银行股份有限公司";
        req = EnterpriseReq.builder()
                .name(company)
                .onlyCounting(false)
                .offset(0)
                .count(5)
                .build();
        Basic basic = enterpriseSearchService.basic(req);
        log.info("basic of {}: {}", company, basic);

        List<AnnualReport> annualReportList = enterpriseSearchService.annualReport(req);
        log.info("annualReport of {}: {}", company, annualReportList);

        Listing listing = enterpriseSearchService.listing(req);
        log.info("listing of {}: {}", company, listing);

        Investment investment = enterpriseSearchService.invest(req);
        log.info("invest of {}: {}", company, investment);

        IntellectualProperty intellectualProperty = enterpriseSearchService.intellectualProperty(req);
        log.info("intellectualProperty of {}: {}", company, intellectualProperty);

        Bidding bidding = enterpriseSearchService.bidding(req);
        log.info("bidding of {}: {}", company, bidding);

        Risk risk = enterpriseSearchService.risk(req);
        log.info("risk of {}: {}", company, risk);

        Sentiment sentiment = enterpriseSearchService.publicSentiment(req);
        log.info("publicSentiment of {}: {}", company, sentiment);

        InvestInstitution investInstitution = enterpriseSearchService.investInstitution(req);
        log.info("investInstitution of {}: {}", company, investInstitution);

    }*/
}
