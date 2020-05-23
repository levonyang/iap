package com.haizhi.iap.follow.model.config.event.macro;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.haizhi.iap.follow.model.Tag;
import com.haizhi.iap.follow.model.config.AbstractConfig;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by chenbo on 2017/12/11.
 */
public abstract class MacroEventConfig extends AbstractConfig {
    @JsonIgnore
    MacroEventType macroType;

    List<Tag> keywords;

    @JsonProperty("keyword_ids")
    List<Number> keywordIds;

    @JsonProperty("type_en_name")
    String typeEnName;

    @Override
    public Integer getType() {
        return this.macroType.getCode();
    }

    @Override
    public String getName() {
        return this.macroType.getName();
    }

    public List<Tag> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<Tag> keywords) {
        this.keywords = keywords;
    }

    public List<Number> getKeywordIds() {
        return keywordIds;
    }

    public void setKeywordIds(List<Number> keywordIds) {
        this.keywordIds = keywordIds;
    }

    public void setTypeEnName(String typeEnName) {
        this.typeEnName = typeEnName;
    }

    public String getTypeEnName() {
        return this.macroType.getTypeEnName();
    }

    @Override
    public Map<String, Object> getParam() {
        Map<String, Object> param = Maps.newHashMap();
        param.put("keywords", getKeywords());
        param.put("keyword_ids", getKeywordIds());
        return param;
    }

    @Override
    public void setParam(Map<String, Object> param) {
        if (param.get("keywords") != null) {
            setKeywords((List<Tag>) param.get("keywords"));
        } else {
            setKeywords(Lists.newArrayList());
        }

        if (param.get("keyword_ids") != null) {
            setKeywordIds((List<Number>) param.get("keyword_ids"));
        } else {
            setKeywordIds(Lists.newArrayList());
        }
    }

    public enum MacroEventType {

        INDUSTRY_NEWS(301, "行业动态", "industry_news"), //行业动态
        AREA_POLICY(302, "政策要闻", "policy_news"), //政策要闻
        BIDDING_DOC(303, "招标公告", "bid_detail_recommend_company");//招标公告

        private Integer code;

        private String name;

        private String typeEnName;

        MacroEventType(Integer code, String name, String typeEnName) {
            this.code = code;
            this.name = name;
            this.typeEnName = typeEnName;
        }

        public Integer getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public String getTypeEnName() {
            return typeEnName;
        }

        public static boolean contains(Integer code) {
            for (MacroEventType type : MacroEventType.values()) {
                if (type.getCode().equals(code)) {
                    return true;
                }
            }
            return false;
        }

        public static List<Integer> allCode() {
            List<Integer> typeList = Lists.newArrayList();
            for (MacroEventType type : MacroEventType.values()) {
                typeList.add(type.getCode());
            }
            return typeList;
        }

        public static Integer getCodeByTypeEnName(String name) {
            for (MacroEventType type : MacroEventType.values()) {
                if (type.getTypeEnName().equals(name))
                    return type.getCode();
            }
            return null;
        }

    }
}
