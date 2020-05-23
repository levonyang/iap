package com.haizhi.iap.search.service;

import java.io.OutputStream;

/**
 * Created by chenbo on 17/2/18.
 */
public interface ExportService {

    OutputStream exportFinancialReport(OutputStream outputStream, String type, String stockCode,
                                       String yearQuarter, String financialTypeInZh, String company);

}
