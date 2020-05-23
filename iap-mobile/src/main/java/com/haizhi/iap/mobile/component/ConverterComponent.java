package com.haizhi.iap.mobile.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * Created by thomas on 18/4/11.
 */
@Component
public class ConverterComponent
{
    @Autowired
    private String2EnumConverterFactory string2EnumConverterFactory;

    @Bean
    public ConversionServiceFactoryBean conversionService()
    {
        ConversionServiceFactoryBean conversionService = new ConversionServiceFactoryBean();
        conversionService.setConverters(Collections.singleton(string2EnumConverterFactory));
        return conversionService;
    }
}
