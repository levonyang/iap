package com.haizhi.iap.search.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Author dmy
 * @Date 2018/2/10 下午12:40.
 */
@Data
@NoArgsConstructor
public class CompanyGroup {

    String groupName;  //实体名称，可能是个人或企业公司名称

    String type;  //族谱信息类型,eg:risk_guarantee_info、risk_black_info、market_updown_info、risk_propagation

    String subType; //族谱子类型，eg:circle

    @JsonProperty("paths")
    String paths;   //图谱路径信息

    List<Map<String, Object>> vertexes;  //顶点信息，非数据库元素，由paths解析而来

    Integer entityCount; //实体数量，包括个人和企业

    Boolean belongInner; //实体是否是行内客户

    Integer innerEntityCount; //行内客户的数量

    Date createTime; //数据插入时间

    Date updateTime; //数据更新时间
}
