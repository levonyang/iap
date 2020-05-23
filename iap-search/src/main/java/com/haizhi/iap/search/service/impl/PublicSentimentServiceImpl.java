package com.haizhi.iap.search.service.impl;

import com.haizhi.iap.common.exception.ServiceAccessException;
import com.haizhi.iap.search.controller.model.DataItem;
import com.haizhi.iap.search.controller.model2.tab.first.PublicSentiment;
import com.haizhi.iap.search.controller.model2.tab.second.BaiduNews;
import com.haizhi.iap.search.exception.SearchException;
import com.haizhi.iap.search.repo.EnterpriseRepo;
import com.haizhi.iap.search.service.PublicSentimentService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by chenbo on 2017/11/9.
 */
@Service
public class PublicSentimentServiceImpl implements PublicSentimentService {
    @Setter
    @Autowired
    EnterpriseRepo enterpriseRepo;

    @Override
    public PublicSentiment getPageBySubType(String companyName, Boolean onlyCount, String subTypeName, Integer offset, Integer count) {
        PublicSentiment sentiment = new PublicSentiment();

        PublicSentiment.SentimentSubType subType = PublicSentiment.SentimentSubType.get(subTypeName);
        if (subType != null) {
            switch (subType){
                case NEWS:
                    sentiment.setBaiduNews(getBaiduNews(companyName, onlyCount, offset, count));
                    break;
                //TODO 关联行业新闻
            }
        }else {
            throw new ServiceAccessException(SearchException.UNSUPPORTED_SUB_TYPE);
        }

        return sentiment;
    }

    @Override
    public PublicSentiment getPageByThirdType(String companyName, Boolean onlyCount, String subTypeName, String thirdType, Integer offset, Integer count) {
        PublicSentiment sentiment = new PublicSentiment();

        PublicSentiment.SentimentSubType subType = PublicSentiment.SentimentSubType.get(subTypeName);
        if (subType != null) {
            if (subType.equals(PublicSentiment.SentimentSubType.NEWS)) {
                BaiduNews.BaiduNewsType newsType = BaiduNews.BaiduNewsType.get(thirdType);
                if (newsType == null) {
                    throw new ServiceAccessException(SearchException.UNSUPPORTED_THIRD_TYPE);
                } else {
                    sentiment.setBaiduNews(getBaiduNews(companyName, newsType, onlyCount, offset, count));
                }
            }
        }
        return sentiment;
    }

    @Override
    public BaiduNews getBaiduNews(String companyName, Boolean onlyCount, Integer offset, Integer count) {
        DataItem positive = new DataItem();
        DataItem negative = new DataItem();
        DataItem neutral = new DataItem();
        if (onlyCount != null && !onlyCount) {
            positive.setData(getBaiduNewsItem(companyName, onlyCount, BaiduNews.BaiduNewsType.POSITIVE, offset, count));
            negative.setData(getBaiduNewsItem(companyName, onlyCount, BaiduNews.BaiduNewsType.NEGATIVE, offset, count));
            neutral.setData(getBaiduNewsItem(companyName, onlyCount, BaiduNews.BaiduNewsType.NEUTRAL, offset, count));
        }
        positive.setTotalCount(enterpriseRepo.countBaiduNewsWithType(companyName, "正面"));
        negative.setTotalCount(enterpriseRepo.countBaiduNewsWithType(companyName, "负面"));
        neutral.setTotalCount(enterpriseRepo.countBaiduNewsWithType(companyName, "中性"));

        BaiduNews baiduNews = new BaiduNews(positive, negative, neutral);
        return baiduNews;
    }

    public BaiduNews getBaiduNews(String companyName, BaiduNews.BaiduNewsType newsType, Boolean onlyCount, Integer offset, Integer count) {
        DataItem positive = new DataItem();
        DataItem negative = new DataItem();
        DataItem neutral = new DataItem();
        switch (newsType) {
            case POSITIVE:
                if(onlyCount != null && !onlyCount){
                    positive.setData(enterpriseRepo.getBaiduNews(companyName, "正面", offset, count));
                }
                positive.setTotalCount(enterpriseRepo.countBaiduNewsWithType(companyName, "正面"));
                break;
            case NEGATIVE:
                if(onlyCount != null && !onlyCount){
                    negative.setData(enterpriseRepo.getBaiduNews(companyName, "负面", offset, count));
                }
                negative.setTotalCount(enterpriseRepo.countBaiduNewsWithType(companyName, "负面"));
                break;
            case NEUTRAL:
                if(onlyCount != null && !onlyCount){
                    neutral.setData(enterpriseRepo.getBaiduNews(companyName, "中性", offset, count));
                }
                neutral.setTotalCount(enterpriseRepo.countBaiduNewsWithType(companyName, "中性"));
                break;
        }

        BaiduNews baiduNews = new BaiduNews(positive, negative, neutral);
        return baiduNews;
    }

    private DataItem getBaiduNewsItem(String companyName, Boolean onlyCount, BaiduNews.BaiduNewsType newsType, Integer offset, Integer count) {
        DataItem news = new DataItem();
        if (onlyCount != null && !onlyCount) {
            switch (newsType) {
                case POSITIVE:
                    news.setData(enterpriseRepo.getBaiduNews(companyName, "正面", offset, count));
                    break;
                case NEGATIVE:
                    news.setData(enterpriseRepo.getBaiduNews(companyName, "负面", offset, count));
                    break;
                case NEUTRAL:
                    news.setData(enterpriseRepo.getBaiduNews(companyName, "中性", offset, count));
                    break;
            }
        }

        switch (newsType) {
            case POSITIVE:
                news.setTotalCount(enterpriseRepo.countBaiduNewsWithType(companyName, "正面"));
                break;
            case NEGATIVE:
                news.setData(enterpriseRepo.countBaiduNewsWithType(companyName, "负面"));
                break;
            case NEUTRAL:
                news.setData(enterpriseRepo.countBaiduNewsWithType(companyName, "中性"));
                break;
        }
        return news;
    }
}
