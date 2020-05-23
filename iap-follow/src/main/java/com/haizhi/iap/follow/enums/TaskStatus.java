package com.haizhi.iap.follow.enums;

/**
 * Created by chenbo on 17/1/13.
 */
public enum TaskStatus {
    WAITING(0), RUNNING(1), FINISHED(2), CANCELED(3), DELETED(4), FAILED(5), PAUSED(6);

    Integer code;

    TaskStatus(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return this.code;
    }

    public static Integer getCode(String modeStr) {
        for (TaskMode mode : TaskMode.values()) {
            if (mode.name().equalsIgnoreCase(modeStr)) {
                return mode.getCode();
            }
        }
        return 0;
    }
}
