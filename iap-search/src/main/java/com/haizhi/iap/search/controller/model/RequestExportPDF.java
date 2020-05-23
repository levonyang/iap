package com.haizhi.iap.search.controller.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by haizhi on 2017/6/26.
 */
//@Data
//@NoArgsConstructor
public class RequestExportPDF {

    private Long userID;

    //@JsonProperty("company")
    private String company;

    //@JsonProperty("name")
    private String name;

    //@JsonProperty("modules")
    private Map<String,List<String>> modules;

    public Map<String, List<String>> getModules() {
        return modules;
    }

    public void setModules(Map<String, List<String>> modules) {
        this.modules = modules;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getUserID() {
        return userID;
    }

    public void setUserID(Long userID) {
        this.userID = userID;
    }
}
