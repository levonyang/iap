package com.haizhi.iap.follow.repo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.haizhi.iap.follow.enums.NotificationType;
import com.haizhi.iap.follow.model.notification.BasicNotification;
import com.haizhi.iap.follow.model.notification.Notification;

/**
 * Created by chenbo on 17/5/3.
 */
@Repository
public class NotificationRepo extends AbstractNotificationRepo<Notification> {

    @Autowired
    @Qualifier(value = "followJdbcTemplate")
    JdbcTemplate template;

    @Autowired
    ObjectMapper objectMapper;

    @Override
    public Notification getNotificationInstance(Integer type) {
    	NotificationType notificationType = NotificationType.get(type);
    	if(notificationType==null) {
    		return null;
    	}
		String cnname = notificationType.getCnName();
		String enname = notificationType.getEnName();
		int code = notificationType.getCode();
		return new BasicNotification(cnname,enname,code);//new NewListShareNotification();
		
		/*if (type.equals(NotificationType.MARKETING_NEW_LISTED_SHAREHOLDER.getCode())) {
    		return new NewListShareNotification();
        } else if (type.equals(NotificationType.MARKETING_TAX_PLAYER_LELVEL_A.getCode())) {
            return new TaxPayerLevelNotification();
        } else if (type.equals(NotificationType.MARKETING_BID_INFO.getCode())) {
            return new BidInfoNotification();
        } else if (type.equals(NotificationType.MARKETING_WIN_BID.getCode())) {
            return new WinBidNotification();
        } else if (type.equals(NotificationType.MARKETING_NEW_AFFILIATE.getCode())) {
            return new NewAffiliateNotification();   
        }else if (type.equals(NotificationType.ENTERPRISE_REGISTERED_CAPITAL_ADD.getCode())) {
            return new RegCapAddNotification();
        }else if (type.equals(NotificationType.ENTERPRISE_FRONT_SENTIMENT.getCode())) {
            return new EnterFrontSentimentNotification();
        }else if (type.equals(NotificationType.ENTERPRISE_HAVEPATENT.getCode())) {
            return new EnterHavePatentNotification();
        }else if (type.equals(NotificationType.ENTERPRISE_MARKET.getCode())) {
            return new EnterMarketNotification();
        }else if (type.equals(NotificationType.ENTERPRISE_HAVEBID.getCode())) {
            return new EnterHaveBidNotification();
        }else if (type.equals(NotificationType.ENTERPRISE_LEGALPERSON_CHANGE.getCode())) {
            return new EnterLegalPersonChangeNotification();
        }else if (type.equals(NotificationType.ENTERPRISE_COMPANYNAME_CHANGE.getCode())) {
            return new EnterCompanyNameNotification();
        }else if (type.equals(NotificationType.ENTERPRISE_BUSSINESSSCOPE_CHANGE.getCode())) {
            return new EnterBussineScopeNotification();
        } else if (type.equals(NotificationType.MARKETING_NEW_INVESTED_COMPANY.getCode())) {
            return new NewInvestNotification();
        } else if (type.equals(NotificationType.RISK_COURT_ANNO.getCode())) {
            return new CourtAnnoNotification();
        } else if (type.equals(NotificationType.RISK_BULLETIN.getCode())) {
            return new BulletinNotification();
        } else if (type.equals(NotificationType.RISK_JUDGE_PROCESS.getCode())) {
            return new JudgeProcessNotification();
        } else if (type.equals(NotificationType.RISK_JUDGEMENT_WENSHU.getCode())) {
            return new JudgeWenshuNotification();
        } else if (type.equals(NotificationType.RISK_SHIXIN_INFO.getCode())) {
            return new ShixinInfoNotification();
        } else if (type.equals(NotificationType.RISK_OWING_TAX.getCode())) {
            return new OwingTaxNotification();
        } else if (type.equals(NotificationType.RISK_PENALTY.getCode())) {
            return new PenaltyNotification();
        } else if (type.equals(NotificationType.RISK_BUSSINESS_STATUS_CHANGE.getCode())) {
            return new BuSiStChangeNotification();
        } else if(type.equals(NotificationType.CLOSTLY_MSG_COUNTOVERVIEW.getCode())){
            return new CloselyMsgNotification();
        } else if(type.equals(NotificationType.RISK_LEGAL_MAN_CHANGE.getCode())) {
            return new LegalManChangeNotification();
        } else if(type.equals(NotificationType.RISK_SENIOR_EXECUTIVE_CHANGE.getCode())) {
            return new SeniorExecutiveChangeNotification();
        } else if (type.equals(NotificationType.RISK_REGISTOR_CAPITAL_CHANGE.getCode())) {
            return new RegistorCapitalChangeNotification();
        } else if(type.equals(NotificationType.RISK_SHAREHOLDER_CHANGE.getCode())) {
            return new ShareholderChangeNotification();
        } else if(type.equals(NotificationType.RISK_PLACE_CHANGE.getCode())) {
            return new PlaceChangeNotification();
        }
        return null;*/
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

    public void deleteAllByMonitor(Long userID, String company) {
        String sql = "update notification set `delete` = true where user_id = ? and ( company = ? or master_company = ? )";
        this.template.update(sql, userID, company, company);
    }
}
