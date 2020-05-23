package com.haizhi.iap.follow.model.notification;

import com.haizhi.iap.follow.enums.NotificationType;

/**
 * Created by haizhi on 2017/9/6.
 */
public class CloselyMsgNotification extends Notification {

    public CloselyMsgNotification() {
        setTypeCnName(NotificationType.get(getType()).getCnName());
        setTypeEnName(NotificationType.get(getType()).getEnName());
    }

    @Override
    public int getType() {
        return NotificationType.CLOSTLY_MSG_COUNTOVERVIEW.getCode();
    }
}
