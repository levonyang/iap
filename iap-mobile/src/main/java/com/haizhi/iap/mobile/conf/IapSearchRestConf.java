package com.haizhi.iap.mobile.conf;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created by thomas on 18/4/18.
 *
 * iap-search相关的restful接口配置信息
 */
@Data
@Component
public class IapSearchRestConf
{
    @Value("${iap.search.base.url}")
    private String baseUrl;

    @Value("${iap.search.company.brief.url}")
    private String companyBriefUrl;

    @Value("${iap.search.graph.cid.url}")
    private String graphCidUrl;

    @Value("${iap.search.graph.cluster.url}")
    private String graphClusterUrl;

    @Value("${iap.search.graph.entityPath.url}")
    private String graphEntityPathUrl;
}
