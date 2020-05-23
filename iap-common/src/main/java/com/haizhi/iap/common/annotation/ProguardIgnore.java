package com.haizhi.iap.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 混淆代码时，需要保留源码，不参与混淆的注解
 * 该注解可以使用到
 * ①、类\接口\枚举：表示整个类\接口\枚举不参与混淆
 * ②、方法：表示方法不参与混淆
 * ③、属性：表示属性不参与混淆
 * @author caochao
 * @Date 2018/8/25
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD})
public @interface ProguardIgnore {

}