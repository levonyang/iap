package com.haizhi.iap.follow.model.notification;

import com.haizhi.iap.follow.enums.NotificationType;

/**
 * Created by chenbo on 17/5/3.
 */
public class NewAffiliateNotification extends Notification {

    public NewAffiliateNotification() {
        setTypeCnName(NotificationType.get(getType()).getCnName());
        setTypeEnName(NotificationType.get(getType()).getEnName());
    }

    @Override
    public int getType() {
        return NotificationType.MARKETING_NEW_AFFILIATE.getCode();
    }

}
