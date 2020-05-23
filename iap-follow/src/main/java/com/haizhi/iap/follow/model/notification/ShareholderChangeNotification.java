package com.haizhi.iap.follow.model.notification;

import com.haizhi.iap.follow.enums.NotificationType;

/**
 * Created by haizhi on 2018/1/2.
 */
public class ShareholderChangeNotification extends Notification {


    public ShareholderChangeNotification() {
        setTypeCnName(NotificationType.get(getType()).getCnName());
        setTypeEnName(NotificationType.get(getType()).getEnName());
    }

    @Override
    public int getType() {
        return NotificationType.RISK_SHAREHOLDER_CHANGE.getCode();
    }

}
