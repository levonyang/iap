package com.haizhi.iap.follow.model.notification;

import com.haizhi.iap.follow.enums.NotificationType;

/**
 * Created by haizhi on 2018/1/2.
 */
public class PlaceChangeNotification extends Notification {

    public PlaceChangeNotification() {
        setTypeCnName(NotificationType.get(getType()).getCnName());
        setTypeEnName(NotificationType.get(getType()).getEnName());
    }

    @Override
    public int getType() {
        return NotificationType.RISK_PLACE_CHANGE.getCode();
    }
}
