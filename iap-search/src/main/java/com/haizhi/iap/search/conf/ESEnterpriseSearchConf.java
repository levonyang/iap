package com.haizhi.iap.search.conf;

import com.haizhi.iap.search.enums.ESEnterpriseSearchType;
import com.haizhi.iap.search.enums.ESSearchMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * Created by chenbo on 16/12/23.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ESEnterpriseSearchConf {
    //搜索列
    private String col;

    //搜索方式
    private ESSearchMethod searchMethod;

    //搜索权重
    private Integer weight;

    private static Map<ESEnterpriseSearchType, List<ESEnterpriseSearchConf>> configs = new HashMap<>();

    static {

        List<ESEnterpriseSearchConf> nameConf = new ArrayList<>();
        nameConf.add(new ESEnterpriseSearchConf("name", ESSearchMethod.MATCH_PHRASE, 60));
        nameConf.add(new ESEnterpriseSearchConf("name.standard", ESSearchMethod.MATCH_PHRASE, 20));
        nameConf.add(new ESEnterpriseSearchConf("used_name_list", ESSearchMethod.MATCH_PHRASE, 40));
        nameConf.add(new ESEnterpriseSearchConf("used_name_list.standard", ESSearchMethod.MATCH_PHRASE, 15));
        nameConf.add(new ESEnterpriseSearchConf(ESEnterpriseSearchType.REGISTERED_CODE.getName(), ESSearchMethod.TERM, 100));
        nameConf.add(new ESEnterpriseSearchConf(ESEnterpriseSearchType.UNIFIED_SOCIAL_CREDIT_CODE.getName(), ESSearchMethod.TERM, 100));

        List<ESEnterpriseSearchConf> personConf = new ArrayList<>();
        personConf.add(new ESEnterpriseSearchConf("key_person.key_person_name", ESSearchMethod.MATCH_PHRASE, 10));
        personConf.add(new ESEnterpriseSearchConf("shareholder_information.shareholder_name", ESSearchMethod.MATCH_PHRASE, 10));
        personConf.add(new ESEnterpriseSearchConf("legal_man", ESSearchMethod.MATCH_PHRASE, 20));

        configs.put(ESEnterpriseSearchType.REGISTERED_CODE,
                Collections.singletonList(
                        new ESEnterpriseSearchConf(ESEnterpriseSearchType.REGISTERED_CODE.getName(), ESSearchMethod.TERM, 100)
                ));

        configs.put(ESEnterpriseSearchType.UNIFIED_SOCIAL_CREDIT_CODE,
                Collections.singletonList(
                        new ESEnterpriseSearchConf(ESEnterpriseSearchType.UNIFIED_SOCIAL_CREDIT_CODE.getName(), ESSearchMethod.TERM, 100)
                ));

        configs.put(ESEnterpriseSearchType.NAME, nameConf);

        configs.put(ESEnterpriseSearchType.KEY_PERSON, personConf);

        configs.put(ESEnterpriseSearchType.TRADEMARK,
                Collections.singletonList(
                        new ESEnterpriseSearchConf("trademark.name", ESSearchMethod.MATCH_PHRASE, 10))
        );

        configs.put(ESEnterpriseSearchType.ADDRESS,
                Collections.singletonList(
                        new ESEnterpriseSearchConf(ESEnterpriseSearchType.ADDRESS.getName(), ESSearchMethod.MATCH_PHRASE, 10))
        );

        configs.put(ESEnterpriseSearchType.BUSINESS_SCOPE,
                Collections.singletonList(
                        new ESEnterpriseSearchConf(ESEnterpriseSearchType.BUSINESS_SCOPE.getName(), ESSearchMethod.MATCH_PHRASE, 10)
                ));
    }

    /**
     * @return
     */
    public static Map<ESEnterpriseSearchType, List<ESEnterpriseSearchConf>> getConfigs() {
        return configs;
    }
}
