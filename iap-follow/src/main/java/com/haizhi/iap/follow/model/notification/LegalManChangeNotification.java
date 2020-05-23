package com.haizhi.iap.follow.model.notification;

import com.haizhi.iap.follow.enums.NotificationType;

/**
 * Created by haizhi on 2018/1/2.
 */
public class LegalManChangeNotification extends Notification {


    public LegalManChangeNotification() {
        setTypeCnName(NotificationType.get(getType()).getCnName());
        setTypeEnName(NotificationType.get(getType()).getEnName());
    }

    @Override
    public int getType() {
        return NotificationType.RISK_LEGAL_MAN_CHANGE.getCode();
    }

}
