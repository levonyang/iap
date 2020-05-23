package com.haizhi.iap.follow.model.config.rule.conduct;

import com.google.common.collect.Lists;
import com.haizhi.iap.follow.model.config.AbstractConfig;

import java.util.List;

/**
 * Created by chenbo on 2017/12/11.
 */
public abstract class ConductConfig extends AbstractConfig {

    ConductType conductType;

    @Override
    public Integer getType() {
        return this.conductType.getCode();
    }

    @Override
    public String getName() {
        return this.conductType.getName();
    }

    public enum ConductType {

        /**
         * 行内关系传导路径
         **/
        /*GUARANTEE_TARGET(401, "担保对象"), GUARANTOR(402, "担保人"), ASSO_GUARANTEE(403, "关联担保方"),

        MONEY_OUT(404, "资金转出对象"), MONEY_IN(405, "资金转入对象"), MONEY_FLOW(406, "资金往来对象"),*/

        /**
         * 行外关系传导路径
         **/
        KEY_SHAREHOLDER(451, "重要股东"), KEY_INVEST(452, "重要投资对象"), BRANCH(453, "分支机构"),

        PARENT_COMPANY(454, "母公司"), ACTUAL_CONTROLLER(455, "实际控制人"), ACTUAL_CONTROL(456, "实际控制对象"),

        KEY_PERSON(457, "高管人事混同对象"),

//        CONCERT(458, "一致行动人"),

        COMPANY_MERGE(459, "疑似同一企业"),

//        SIMILARITY_COMPANY(460, "相似企业"),

        COMMON_LAWSUITS(461, "共同涉诉方");

//        UPSTREAM_COMPANY(462, "上游企业"),
//
//        DOWNSTREAM_COMPANY(463, "下游企业");

        private Integer code;

        private String name;

        ConductType(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        public Integer getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public static boolean contains(Integer code) {
            for (ConductType type : ConductType.values()) {
                if (type.getCode().equals(code)) {
                    return true;
                }
            }
            return false;
        }

        public static List<Integer> allCode() {
            List<Integer> innerTypeList = Lists.newArrayList();
            for (ConductType type : ConductType.values()) {
                innerTypeList.add(type.getCode());
            }
            return innerTypeList;

        }
    }
}
