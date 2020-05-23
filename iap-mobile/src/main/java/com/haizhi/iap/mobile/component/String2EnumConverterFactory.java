package com.haizhi.iap.mobile.component;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.stereotype.Component;

/**
 * Created by thomas on 18/4/11.
 *
 * 实现spring mvc入参 string转enum
 */
@Component
public class String2EnumConverterFactory implements ConverterFactory<String, Enum>
{
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Enum> Converter<String, T> getConverter(Class<T> cls)
    {
        return name -> StringUtils.isBlank(name) ? null : (T) Enum.valueOf(cls, name.toUpperCase());
    }
}
