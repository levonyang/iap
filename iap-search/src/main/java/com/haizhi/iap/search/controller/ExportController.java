package com.haizhi.iap.search.controller;

import com.google.common.base.Strings;
import com.haizhi.iap.common.exception.ServiceAccessException;
import com.haizhi.iap.search.conf.FinancialReportItem;
import com.haizhi.iap.search.conf.ListedCompanyFormat;
import com.haizhi.iap.search.enums.FinancialReportField;
import com.haizhi.iap.search.exception.SearchException;
import com.haizhi.iap.search.service.ExportService;
import com.haizhi.iap.search.utils.DateUtils;
import io.swagger.annotations.Api;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.util.Date;

/**
 * Created by chenbo on 17/2/17.
 */
@Api(tags="【搜索-导出模块】导出财报")
@Slf4j
@RestController
@RequestMapping(value = "/search/export")
public class ExportController {

    @Setter
    @Autowired
    ExportService exportService;

    @RequestMapping(value = "/financial_report", method = RequestMethod.GET)
    public ResponseEntity<byte[]> exportFinancialReport(@RequestParam("type") String type,
                                                        @RequestParam("stock_code") String stockCode,
                                                        @RequestParam("year_quarter") String yearQuarter,
                                                        @RequestParam("caibao_type") String financialTypeInZh,
                                                        @RequestParam("company") String company) {
        if (Strings.isNullOrEmpty(type)) {
            throw new ServiceAccessException(SearchException.MISS_TYPE);
        }
        if (Strings.isNullOrEmpty(stockCode)) {
            throw new ServiceAccessException(SearchException.MISS_STOCK_CODE);
        }

        if (Strings.isNullOrEmpty(yearQuarter)) {
            throw new ServiceAccessException(SearchException.MISS_YEAR_QUARTER);
        }
        if (Strings.isNullOrEmpty(financialTypeInZh)) {
            throw new ServiceAccessException(SearchException.MISS_CAIBAO_TYPE);
        }
        if (Strings.isNullOrEmpty(company)) {
            throw new ServiceAccessException(SearchException.MISS_COMPANY);
        }

        FinancialReportItem exportItem = new FinancialReportItem();
        if (type.equals(FinancialReportField.COMPANY_ABILITY.getName())) {
            exportItem = ListedCompanyFormat.getCompanyAbility();
        } else if (type.equals(FinancialReportField.CASH_FLOW.getName())) {
            exportItem = ListedCompanyFormat.getCashFlow();
        } else if (type.equals(FinancialReportField.ASSETS_LIABILITY.getName())) {
            exportItem = ListedCompanyFormat.getAssetsLiability();
        } else if (type.equals(FinancialReportField.PROFIT.getName())) {
            exportItem = ListedCompanyFormat.getProfit();
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        exportService.exportFinancialReport(output, type, stockCode, yearQuarter, financialTypeInZh, company);

        String filename = company + "_" + yearQuarter + "_" + exportItem.getItem() + "_" + DateUtils.FORMAT_DATE.format(new Date()) + ".pdf";
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentDispositionFormData("attachment", java.net.URLEncoder.encode(filename, "UTF-8"));
            httpHeaders.setContentType(MediaType.parseMediaType("application/pdf"));
            return new ResponseEntity<byte[]>(output.toByteArray(),
                    httpHeaders,
                    HttpStatus.OK);
        } catch (Exception e) {
            log.error("{}", e);
        }
        return null;
    }

}
