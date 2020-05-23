package com.haizhi.iap.follow.model.notification;

import com.haizhi.iap.follow.enums.NotificationType;

/**
 * Created by chenbo on 17/5/8.
 */
public class BuSiStChangeNotification extends Notification {

    public BuSiStChangeNotification() {
        setTypeCnName(NotificationType.get(getType()).getCnName());
        setTypeEnName(NotificationType.get(getType()).getEnName());
    }

    @Override
    public int getType() {
        return NotificationType.RISK_BUSSINESS_STATUS_CHANGE.getCode();
    }

}
