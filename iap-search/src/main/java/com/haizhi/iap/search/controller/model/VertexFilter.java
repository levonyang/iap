package com.haizhi.iap.search.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by chenbo on 17/2/23.
 */
@Data
@NoArgsConstructor
public class VertexFilter {
    @JsonProperty("Company")
    CompanyFilter company;

    @JsonProperty("Bid_detail")
    BidDetailFilter bid_detail;

    @JsonProperty("Judgement_wenshu")
    JudgementWenshuFilter judgementWenshu;

    @JsonProperty("Court_bulletin_doc")
    CourtBulletinDocFilter courtBulletinDoc;

    @JsonProperty("Judge_process")
    JudgeProcessFilter judgeProcess;

    @JsonProperty("Court_announcement_doc")
    CourtAnnouncementDoc courtAnnouncementDoc;
}
