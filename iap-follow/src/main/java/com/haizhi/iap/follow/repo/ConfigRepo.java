package com.haizhi.iap.follow.repo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.haizhi.iap.follow.enums.NotificationType;
import com.haizhi.iap.follow.model.ChanceSeaInfo;
import com.haizhi.iap.follow.model.config.AbstractConfig;
import com.haizhi.iap.follow.model.config.event.macro.AreaPolicyConfig;
import com.haizhi.iap.follow.model.config.event.macro.BiddingDocConfig;
import com.haizhi.iap.follow.model.config.event.macro.IndustryNewsConfig;
import com.haizhi.iap.follow.model.config.event.macro.MacroEventConfig;
import com.haizhi.iap.follow.model.config.event.market.*;
import com.haizhi.iap.follow.model.config.event.risk.*;
import com.haizhi.iap.follow.model.config.rule.conduct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by chenbo on 2017/12/11.
 */
@Repository
public class ConfigRepo extends AbstractConfigRepo<AbstractConfig> {

    @Autowired
    @Qualifier(value = "followJdbcTemplate")
    JdbcTemplate template;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    ObjectMapper objectMapper;

    private static final String TABLE_CHANCE_SEA = "chance_sea";


    @Override
    public AbstractConfig getConfigInstance(Integer type) {
        AbstractConfig config = null;
        if (type.equals(RiskEventConfig.RiskEventType.COURT_KTGG.getCode())) {
            config = new CourtKtggConfig();
        } else if (type.equals(RiskEventConfig.RiskEventType.COURT_FYGG.getCode())) {
            config = new CourtFyggConfig();
        } else if (type.equals(RiskEventConfig.RiskEventType.JUDGE_PROCESS.getCode())) {
            config = new JudgeProcessConfig();
        } else if (type.equals(RiskEventConfig.RiskEventType.JUDGEMENT.getCode())) {
            config = new JudgementConfig();
        } else if (type.equals(RiskEventConfig.RiskEventType.SHIXIN_INFO.getCode())) {
            config = new ShixinConfig();
        }
        /*else if (type.equals(RiskEventConfig.RiskEventType.OWING_TAX.getCode())) {
            config = new OwingTaxConfig();
        } */
        else if (type.equals(RiskEventConfig.RiskEventType.PENALTY.getCode())) {
            config = new PenaltyConfig();
        } else if (type.equals(RiskEventConfig.RiskEventType.BUSINESS_STATUS_UNUSUAL.getCode())) {
            config = new BusinessStatusUnusualConfig();
        } else if (type.equals(RiskEventConfig.RiskEventType.LEGAL_MAN_CHANGE.getCode())) {
            config = new LegalManChangeConfig();
        } else if (type.equals(RiskEventConfig.RiskEventType.MANAGER_CHANGE.getCode())) {
            config = new ManagerChangeConfig();
        }
        /*else if (type.equals(RiskEventConfig.RiskEventType.REGISTER_CAPITAL_CHANGE.getCode())) {
            config = new RegisterCapitalChangeConfig();
        }*/
        else if (type.equals(RiskEventConfig.RiskEventType.SHAREHOLDER_CHANGE.getCode())) {
            config = new ShareholderChangeConfig();
        }
        /*else if (type.equals(RiskEventConfig.RiskEventType.NAME_CHANGE.getCode())) {
            config = new NameChangeConfig();
        } */
        else if (type.equals(RiskEventConfig.RiskEventType.ADDRESS_CHANGE.getCode())) {
            config = new AddressChangeConfig();
        }
        /*else if (type.equals(RiskEventConfig.RiskEventType.BLIND_EXPAND.getCode())) {
            config = new BlindExpandConfig();
        }*/
        //===================================营销==================================
        else if (type.equals(MarketEventConfig.MarketEventType.SHAREHOLDER_LISTED.getCode())) {
            config = new ShareholderListedConfig();
        } else if (type.equals(MarketEventConfig.MarketEventType.TAX_LEVEL_A.getCode())) {
            config = new TaxLevelAConfig();
        } else if (type.equals(MarketEventConfig.MarketEventType.BIDDING.getCode())) {
            config = new BiddingConfig();
        } else if (type.equals(MarketEventConfig.MarketEventType.NEW_BRANCH.getCode())) {
            config = new NewBranchConfig();
        } else if (type.equals(MarketEventConfig.MarketEventType.NEW_INVEST.getCode())) {
            config = new NewInvestConfig();
        }
        //===================================宏观===================================
        else if (type.equals(MacroEventConfig.MacroEventType.INDUSTRY_NEWS.getCode())) {
            config = new IndustryNewsConfig();
        } else if (type.equals(MacroEventConfig.MacroEventType.AREA_POLICY.getCode())) {
            config = new AreaPolicyConfig();
        } else if (type.equals(MacroEventConfig.MacroEventType.BIDDING_DOC.getCode())) {
            config = new BiddingDocConfig();
        }
        //===================================传导===================================
        /*else if (type.equals(ConductConfig.ConductType.GUARANTEE_TARGET.getCode())) {
            config = new GuaranteeTargetConfig();
        } else if (type.equals(ConductConfig.ConductType.GUARANTOR.getCode())) {
            config = new GuarantorConfig();
        } else if (type.equals(ConductConfig.ConductType.ASSO_GUARANTEE.getCode())) {
            config = new AssoGuaranteeConfig();
        } else if (type.equals(ConductConfig.ConductType.MONEY_OUT.getCode())) {
            config = new MoneyOutConfig();
        } else if (type.equals(ConductConfig.ConductType.MONEY_IN.getCode())) {
            config = new MoneyInConfig();
        } else if (type.equals(ConductConfig.ConductType.MONEY_FLOW.getCode())) {
            config = new MoneyFlowConfig();
        } */
        else if (type.equals(ConductConfig.ConductType.KEY_SHAREHOLDER.getCode())) {
            config = new KeyShareholderConfig();
        } else if (type.equals(ConductConfig.ConductType.KEY_INVEST.getCode())) {
            config = new KeyInvestConfig();
        } else if (type.equals(ConductConfig.ConductType.BRANCH.getCode())) {
            config = new BranchConfig();
        } else if (type.equals(ConductConfig.ConductType.PARENT_COMPANY.getCode())) {
            config = new ParentCompanyConfig();
        } else if (type.equals(ConductConfig.ConductType.ACTUAL_CONTROLLER.getCode())) {
            config = new ActualControllerConfig();
        } else if (type.equals(ConductConfig.ConductType.ACTUAL_CONTROL.getCode())) {
            config = new ActualControlConfig();
        } else if (type.equals(ConductConfig.ConductType.KEY_PERSON.getCode())) {
            config = new KeyPersonConfig();
        }
        /*else if (type.equals(ConductConfig.ConductType.CONCERT.getCode())) {
            config = new ConcertConfig();
        }*/
        else if (type.equals(ConductConfig.ConductType.COMPANY_MERGE.getCode())) {
            config = new CompanyMergeConfig();
        }
        /*else if (type.equals(ConductConfig.ConductType.SIMILARITY_COMPANY.getCode())) {
            config = new SimilarityCompanyConfig();
        } */
        else if (type.equals(ConductConfig.ConductType.COMMON_LAWSUITS.getCode())) {
            config = new CommonLawsuitsConfig();
        }
        /*else if (type.equals(ConductConfig.ConductType.UPSTREAM_COMPANY.getCode())) {
            config = new UpstreamCompanyConfig();
        } else if (type.equals(ConductConfig.ConductType.DOWNSTREAM_COMPANY.getCode())) {
            config = new DownstreamCompanyConfig();
        }*/

        //初始化
        if(config != null){
            config.setEnable(1);
            config.setParam(Collections.emptyMap());
        }
        return config;
    }

    @Override
    public void setTemplate(JdbcTemplate template) {
        super.setTemplate(template);
        this.template = template;
    }

    @Override
    public void setObjectMapper(ObjectMapper objectMapper) {
        super.setObjectMapper(objectMapper);
        this.objectMapper = objectMapper;
    }

    public List<ChanceSeaInfo> getChanceSeaCollectedList(List<Long> idList) {
        MapSqlParameterSource parameterSource = new MapSqlParameterSource();

        parameterSource.addValue("idList", idList);

        String sql = "select id,title,rule_name,company,master_company,type,`desc`,relationship,level,push_time from chance_sea where id in (:idList) order by push_time desc";

        List<ChanceSeaInfo> list = this.namedParameterJdbcTemplate.query(sql, parameterSource, new ChanceSeaInfoRowMapper());
        buildNotifycationType(list);
        return list;
    }

    public Long getChanceSeaCollectedCount(Long userID) {
        String sql = "select count(1) from chance_sea_collected where user_id = ?";
        Long count = this.template.queryForObject(sql, Long.TYPE, new Object[] {userID});
        return count;
    }

    public List<Long> getChanceSeaCollectedIDList(Long userID, Integer limit, Integer offset) {
        String sql = "select chance_sea_id from chance_sea_collected where user_id = ? order by create_time desc limit ? , ? ";
        List<Long> idList = this.template.queryForList(sql, new Object[] {userID, offset, limit}, Long.TYPE);
        return idList;
    }

    public List<ChanceSeaInfo> getChanceSeaInfoList(Long userID, Integer limit, Integer offset) {
        String sql = "select a.id,title,rule_name,company,master_company,type,`desc`,relationship,level,push_time,\n" +
                "if(b.user_id,true,false) collected \n" +
                " from chance_sea a left join (\n" +
                " select *from chance_sea_collected where user_id = ?\n" +
                " ) b on a.id = b.chance_sea_id order by push_time desc limit ? , ? ";
        List list = new ArrayList();
        list.add(userID);
        list.add(offset);
        list.add(limit);

        List<ChanceSeaInfo> data = this.template.query(sql, new ChanceSeaInfoRowMapper(), list.toArray());
        buildNotifycationType(data);
        return data;
    }

    public Long getChanceSeaInfoCount() {
        StringBuilder sql = new StringBuilder("select count(1) from chance_sea");

        Long count = this.template.queryForObject(sql.toString(), Long.TYPE);
        return count;
    }

    public ChanceSeaInfo getChanceSeaInfoDetail(Long id) {
        StringBuilder sql = new StringBuilder("select id,title,rule_name,company,master_company,type,`desc`,relationship,level,detail,push_time from chance_sea where id = ? ");

        List<ChanceSeaInfo> data = this.template.query(sql.toString(), new ChanceSeaInfoRowMapper(), new Object[]{id});
        buildNotifycationType(data);
        return data.get(0);
    }

    private void buildNotifycationType(List<ChanceSeaInfo> list) {
        NotificationType noType = null;
        if (list != null) {
            for (ChanceSeaInfo chanceSeaInfo : list) {
                noType = NotificationType.get(chanceSeaInfo.getType());
                if (noType != null) {
                    chanceSeaInfo.setTypeCnName(noType.getCnName());
                    chanceSeaInfo.setTypeEnName(noType.getEnName());
                }
            }
        }
    }

    private class ChanceSeaInfoRowMapper implements RowMapper<ChanceSeaInfo> {
        @Override
        public ChanceSeaInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            ChanceSeaInfo info = new ChanceSeaInfo();
            info.setId(rs.getLong("id"));
            info.setCompany(rs.getString("company"));
            info.setMasterCompany(rs.getString("master_company"));
            info.setRuleName(rs.getString("rule_name"));
            info.setLevel(rs.getString("level"));
            info.setPushTime(rs.getString("push_time"));
            info.setDesc(rs.getString("desc"));
            info.setRelationship(rs.getString("relationship"));
            info.setType(rs.getInt("type"));
            info.setTitle(rs.getString("title"));
            try {
                info.setCollected(rs.getBoolean("collected"));
            } catch (Exception e) {}
            try {
                info.setDetail(rs.getString("detail"));
            } catch (Exception e) {}
            return info;
        }
    }

    public void changeChanceSeaStatus(Long id, Long userID, boolean collected) {
        if (collected) {
            String timeStr = this.template.queryForObject("select push_time from chance_sea where id = ?",
                    String.class, id);
            this.template.update("insert into chance_sea_collected(chance_sea_id,user_id,create_time) values(?,?,?)",
                    id, userID, timeStr);
        } else {
            this.template.update("delete from chance_sea_collected where chance_sea_id = ? and user_id = ? ",
                    id, userID);
        }
    }
}
