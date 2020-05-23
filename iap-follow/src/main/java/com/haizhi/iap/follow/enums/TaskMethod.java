package com.haizhi.iap.follow.enums;

/**
 * Created by chenbo on 17/1/12.
 */
public enum TaskMethod {
    ONCE(0), PERIOD(1);

    Integer code;

    TaskMethod(Integer code) {
        this.code = code;
    }

    public Integer getCode(){
        return this.code;
    }

    public static Integer getCode(String mode){
        for(TaskMethod method : TaskMethod.values()){
            if(method.name().equalsIgnoreCase(mode)){
                return method.getCode();
            }
        }
        return 0;
    }
}
