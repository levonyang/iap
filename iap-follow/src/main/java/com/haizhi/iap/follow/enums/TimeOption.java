package com.haizhi.iap.follow.enums;

/**
 * Created by chenbo on 17/1/12.
 */
public enum TimeOption {
    IN_TIME(0), DATA_TIME(1);

    Integer code;

    TimeOption(Integer code) {
        this.code = code;
    }

    public static Integer getCode(String mode){
        for(TimeOption option : TimeOption.values()){
            if(option.name().equalsIgnoreCase(mode)){
                return option.code;
            }
        }
        return 0;
    }

    public Integer getCode(){
        return this.code;
    }

    public String getName(){
        return this.name().toLowerCase();
    }
}
