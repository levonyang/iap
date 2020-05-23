package com.haizhi.iap.search.service.impl;

import com.haizhi.iap.common.exception.ServiceAccessException;
import com.haizhi.iap.search.controller.model.DataItem;
import com.haizhi.iap.search.controller.model2.tab.first.MarketInfo;
import com.haizhi.iap.search.exception.SearchException;
import com.haizhi.iap.search.repo.EnterpriseRepo;
import com.haizhi.iap.search.service.MarketInfoService;
import com.haizhi.iap.search.utils.PageUtil;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by chenbo on 2017/11/9.
 */
@Service
public class MarketServiceImpl implements MarketInfoService {
    @Setter
    @Autowired
    EnterpriseRepo enterpriseRepo;

    @Override
    public MarketInfo getPageBySubType(String companyName, Boolean onlyCount, String subTypeName,
                                       Map<String, Object> basic, Integer offset, Integer count) {
        MarketInfo.MarketSubType subType = MarketInfo.MarketSubType.get(subTypeName);
        if (subType == null) {
            throw new ServiceAccessException(SearchException.UNSUPPORTED_SUB_TYPE);
        } else {
            MarketInfo marketInfo = new MarketInfo();

            switch (subType) {
                case BID_INFO:
                    marketInfo.setBidInfo(getBidInfo(companyName, onlyCount, offset, count));
                    break;
                case WIN_INFO:
                    marketInfo.setWinInfo(getWinInfo(companyName, onlyCount, offset, count));
                    break;
                case LAND_AUCTION:
                    marketInfo.setLandAuction(getLandAuction(companyName, onlyCount, offset, count));
                    break;
                case PATENT:
                    marketInfo.setPatent(getPatent(companyName, onlyCount, offset, count));
                    break;
                case TAX_PAYER_LEVEL_A:
                    marketInfo.setTaxPayerLevelA(getTaxPayerLevelA(companyName, onlyCount, offset, count));
                    break;
                case HIRING_INFO:
                    marketInfo.setHiringInfo(getHiringInfo(companyName, onlyCount, offset, count));
                    break;
            }
            return marketInfo;
        }
    }

    /**
     * 招标信息
     * @param companyName
     * @param onlyCount
     * @param offset
     * @param count
     * @return
     */
    @Override
    public DataItem getBidInfo(String companyName, Boolean onlyCount, Integer offset, Integer count) {
        DataItem bidInfo = new DataItem();
        if (onlyCount != null && !onlyCount) {
            List<Map> bidInfoData = enterpriseRepo.getBidInfo(companyName, offset, count);
            bidInfo.setData(bidInfoData);
        }
        bidInfo.setTotalCount(enterpriseRepo.countBidInfo(companyName));
        return bidInfo;
    }

    /**
     * 中标信息
     * @param companyName
     * @param onlyCount
     * @param offset
     * @param count
     * @return
     */
    @Override
    public DataItem getWinInfo(String companyName, Boolean onlyCount, Integer offset, Integer count) {
        DataItem winInfo = new DataItem();
        if (onlyCount != null && !onlyCount) {
            List<Map> winInfoData = enterpriseRepo.getWinInfo(companyName, offset, count);
            winInfo.setData(winInfoData);
        }
        winInfo.setTotalCount(enterpriseRepo.countWinInfo(companyName));
        return winInfo;
    }

    /**
     * 土地招拍挂信息
     * @param companyName
     * @param onlyCount
     * @param offset
     * @param count
     * @return
     */
    @Override
    public DataItem getLandAuction(String companyName, Boolean onlyCount, Integer offset, Integer count) {
        DataItem landAuction = new DataItem();
        if (onlyCount != null && !onlyCount) {
            List<Map> landAuctionData = enterpriseRepo.getLandAuction(companyName, offset, count);
            landAuction.setData(landAuctionData);
        }
        landAuction.setTotalCount(enterpriseRepo.countLandAuction(companyName));
        return landAuction;
    }

    /**
     * 专利信息
     * @param companyName
     * @param onlyCount
     * @param offset
     * @param count
     * @return
     */
    @Override
    public DataItem getPatent(String companyName, Boolean onlyCount, Integer offset, Integer count) {
        DataItem patent = new DataItem();
        if (onlyCount != null && !onlyCount) {
            patent.setData(enterpriseRepo.getPatent(companyName, offset, count));
        }
        patent.setTotalCount(enterpriseRepo.countPatent(companyName));
        return patent;
    }

    /**
     * 纳税等级为A
     * @param companyName
     * @param onlyCount
     * @param offset
     * @param count
     * @return
     */
    @Override
    public DataItem getTaxPayerLevelA(String companyName, Boolean onlyCount, Integer offset, Integer count) {
        DataItem taxPayerLevelA = new DataItem();
        if (onlyCount != null && !onlyCount) {
            List<Map> taxPayerLevelAData = enterpriseRepo.getTaxRank(companyName, offset, count);
            taxPayerLevelA.setData(taxPayerLevelAData);
        }
        taxPayerLevelA.setTotalCount(enterpriseRepo.countTaxRank(companyName));
        return taxPayerLevelA;
    }

    @Override
    public DataItem getHiringInfo(String companyName, Boolean onlyCount, Integer offset, Integer count) {
        DataItem hiringInfo = new DataItem();
        if (onlyCount != null && !onlyCount) {
            List<Map> hiringInfoData = enterpriseRepo.getHiringInfo(companyName, offset, count);
            hiringInfo.setData(hiringInfoData);
        }
        hiringInfo.setTotalCount(enterpriseRepo.countHiringInfo(companyName));
        return hiringInfo;
    }


}
