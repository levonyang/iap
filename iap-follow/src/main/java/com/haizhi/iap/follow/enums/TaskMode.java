package com.haizhi.iap.follow.enums;

/**
 * Created by chenbo on 17/1/12.
 */
public enum TaskMode {
    ON(0), OFF(1);

    Integer code;

    TaskMode(Integer code) {
        this.code = code;
    }

    public Integer getCode(){
        return this.code;
    }

    public static Integer getCode(String modeStr){
        for(TaskMode mode : TaskMode.values()){
            if (mode.name().equalsIgnoreCase(modeStr)){
                return mode.getCode();
            }
        }
        return 0;
    }
}
