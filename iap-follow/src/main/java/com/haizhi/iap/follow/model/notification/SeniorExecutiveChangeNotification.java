package com.haizhi.iap.follow.model.notification;

import com.haizhi.iap.follow.enums.NotificationType;

/**
 * Created by haizhi on 2018/1/2.
 */
public class SeniorExecutiveChangeNotification extends Notification {

    public SeniorExecutiveChangeNotification() {
        setTypeCnName(NotificationType.get(getType()).getCnName());
        setTypeEnName(NotificationType.get(getType()).getEnName());
    }

    @Override
    public int getType() {
        return NotificationType.RISK_SENIOR_EXECUTIVE_CHANGE.getCode();
    }
}
