package com.haizhi.iap.common.exception;

import com.haizhi.iap.common.Wrapper;
import com.haizhi.iap.common.WrapperProvider;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ServiceAccessException extends RuntimeException implements WrapperProvider {

    private Wrapper wrapper;

    public ServiceAccessException(int status, String msg) {
        this(Wrapper.builder().status(status).msg(msg).build());
    }

    public ServiceAccessException(Wrapper wrapper) {
        this.wrapper = wrapper;
    }

    public ServiceAccessException(WrapperProvider provider) {
        this.wrapper = provider.get();
    }

    @Override
    public Wrapper get() {
        return wrapper;
    }
}
