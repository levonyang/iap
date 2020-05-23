package com.haizhi.iap.search.repo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import com.haizhi.iap.common.exception.ServiceAccessException;
import com.haizhi.iap.search.exception.SearchException;
import com.haizhi.iap.search.controller.model.DataItem;
import com.haizhi.iap.search.model.Tab;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Created by chenbo on 17/3/1.
 */
@Slf4j
public class EnterpriseRepoUtil {
    @Deprecated
    public static List<Map> get(Class entityClazz, String jsonProperty, EnterpriseRepo enterpriseRepo,
                                String key, Integer offset, Integer count) {
        if (Strings.isNullOrEmpty(jsonProperty) || enterpriseRepo == null) {
            return null;
        }
        try {
            for (Field field : entityClazz.getDeclaredFields()) {

                field.setAccessible(true);
                String firstLetter = field.getName().substring(0, 1).toUpperCase();

                String getMethodName;
                Method getMethod;
                if (field.getDeclaredAnnotation(JsonProperty.class) != null) {
                    String jsonPropertyAnn = field.getDeclaredAnnotation(JsonProperty.class).value();
                    if (jsonProperty.equals(jsonPropertyAnn)) {
                        getMethodName = "get" + firstLetter + field.getName().substring(1);
                        getMethod = EnterpriseRepo.class.getDeclaredMethod(getMethodName, String.class, Integer.class, Integer.class);
                        return (List<Map>) getMethod.invoke(enterpriseRepo, key, offset, count);
                    }
                } else if (field.getName().equals(jsonProperty)) {
                    getMethodName = "get" + firstLetter + field.getName().substring(1);
                    getMethod = EnterpriseRepo.class.getDeclaredMethod(getMethodName, String.class, Integer.class, Integer.class);
                    return (List<Map>) getMethod.invoke(enterpriseRepo, key, offset, count);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static DataItem get(Class entityClazz, String jsonProperty, EnterpriseRepo enterpriseRepo,
                               String key, String tab, Integer offset, Integer count) {
        if (Strings.isNullOrEmpty(jsonProperty) || enterpriseRepo == null) {
            return null;
        }
        try {
            for (Field field : entityClazz.getDeclaredFields()) {

                field.setAccessible(true);
                String firstLetter = field.getName().substring(0, 1).toUpperCase();

                String getMethodName;
                Method getMethod;
                if (field.getDeclaredAnnotation(JsonProperty.class) != null) {
                    String jsonPropertyAnn = field.getDeclaredAnnotation(JsonProperty.class).value();
                    if (jsonProperty.equals(jsonPropertyAnn)) {
                        getMethodName = "get" + firstLetter + field.getName().substring(1);
                        //有tab选项卡做anchor
                        if (field.getDeclaredAnnotation(Tab.class) != null) {
                            getMethod = EnterpriseRepo.class.getDeclaredMethod(getMethodName, String.class, String.class, Integer.class, Integer.class);
                            return (DataItem) getMethod.invoke(enterpriseRepo, key, tab, offset, count);
                        } else {
                            getMethod = EnterpriseRepo.class.getDeclaredMethod(getMethodName, String.class, Integer.class, Integer.class);
                            Object data = getMethod.invoke(enterpriseRepo, key, offset, count);

                            String countMethodName = "count" + firstLetter + field.getName().substring(1);
                            Method countMethod = EnterpriseRepo.class.getDeclaredMethod(countMethodName, String.class);
                            Long countResult = (Long) countMethod.invoke(enterpriseRepo, key);
                            return new DataItem(data, countResult);
                        }
                    }
                } else if (field.getName().equals(jsonProperty)) {
                    //有tab选项卡做anchor
                    getMethodName = "get" + firstLetter + field.getName().substring(1);

                    if (field.getDeclaredAnnotation(Tab.class) != null) {
                        getMethod = EnterpriseRepo.class.getDeclaredMethod(getMethodName, String.class, String.class, Integer.class, Integer.class);
                        return (DataItem) getMethod.invoke(enterpriseRepo, key, tab, offset, count);
                    } else {
                        getMethod = EnterpriseRepo.class.getDeclaredMethod(getMethodName, String.class, Integer.class, Integer.class);
                        Object data = getMethod.invoke(enterpriseRepo, key, offset, count);

                        String countMethodName = "count" + firstLetter + field.getName().substring(1);
                        Method countMethod = EnterpriseRepo.class.getDeclaredMethod(countMethodName, String.class);
                        Long countResult = (Long) countMethod.invoke(enterpriseRepo, key);
                        return new DataItem(data, countResult);
                    }

                }
            }
        } catch (Exception e) {
            if (e instanceof NoSuchMethodException) {
                throw new ServiceAccessException(SearchException.NOT_PAGEABLE);
            } else {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static DataItem getFromList(Class entityClazz, String jsonProperty, EnterpriseRepo enterpriseRepo,
                               List<Map> list, Integer offset, Integer count) {
        if (Strings.isNullOrEmpty(jsonProperty) || enterpriseRepo == null) {
            return null;
        }
        try {
            for (Field field : entityClazz.getDeclaredFields()) {

                field.setAccessible(true);
                String firstLetter = field.getName().substring(0, 1).toUpperCase();

                String getMethodName;
                Method getMethod;
                if (field.getDeclaredAnnotation(JsonProperty.class) != null) {
                    String jsonPropertyAnn = field.getDeclaredAnnotation(JsonProperty.class).value();
                    if (jsonProperty.equals(jsonPropertyAnn)) {
                        getMethodName = "get" + firstLetter + field.getName().substring(1);

                        getMethod = EnterpriseRepo.class.getDeclaredMethod(getMethodName, List.class, Integer.class, Integer.class);
                        Object data = getMethod.invoke(enterpriseRepo, list, offset, count);

                        String countMethodName = "count" + firstLetter + field.getName().substring(1);
                        Method countMethod = EnterpriseRepo.class.getDeclaredMethod(countMethodName, List.class);
                        Long countResult = (Long) countMethod.invoke(enterpriseRepo, list);
                        return new DataItem(data, countResult);
                    }
                } else if (field.getName().equals(jsonProperty)) {
                    //有tab选项卡做anchor
                    getMethodName = "get" + firstLetter + field.getName().substring(1);

                    getMethod = EnterpriseRepo.class.getDeclaredMethod(getMethodName, List.class, Integer.class, Integer.class);
                    Object data = getMethod.invoke(enterpriseRepo, list, offset, count);

                    String countMethodName = "count" + firstLetter + field.getName().substring(1);
                    Method countMethod = EnterpriseRepo.class.getDeclaredMethod(countMethodName, List.class);
                    Long countResult = (Long) countMethod.invoke(enterpriseRepo, list);
                    return new DataItem(data, countResult);
                }
            }
        } catch (Exception e) {
            if (e instanceof NoSuchMethodException) {
                log.error("{}", e);
                throw new ServiceAccessException(SearchException.NOT_PAGEABLE);
            } else {
                log.error("{}", e);
            }
        }
        return null;
    }

    public static Long count(Class entityClazz, String jsonProperty, EnterpriseRepo enterpriseRepo, String key) {
        if (Strings.isNullOrEmpty(jsonProperty) || enterpriseRepo == null) {
            return null;
        }
        try {
            for (Field field : entityClazz.getDeclaredFields()) {

                field.setAccessible(true);
                String firstLetter = field.getName().substring(0, 1).toUpperCase();

                String countMethodName;
                Method getMethod;
                if (field.getDeclaredAnnotation(JsonProperty.class) != null) {
                    String jsonPropertyAnn = field.getDeclaredAnnotation(JsonProperty.class).value();
                    if (jsonProperty.equals(jsonPropertyAnn)) {
                        countMethodName = "count" + firstLetter + field.getName().substring(1);
                        getMethod = EnterpriseRepo.class.getDeclaredMethod(countMethodName, String.class);
                        return (Long) getMethod.invoke(enterpriseRepo, key);
                    }
                } else if (field.getName().equals(jsonProperty)) {
                    countMethodName = "count" + firstLetter + field.getName().substring(1);
                    getMethod = EnterpriseRepo.class.getDeclaredMethod(countMethodName, String.class);
                    return (Long) getMethod.invoke(enterpriseRepo, key);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T set(Class<T> entityClazz, String jsonProperty, Object... args) {
        if (Strings.isNullOrEmpty(jsonProperty)) {
            return null;
        }

        try {
            T bean = entityClazz.newInstance();
            for (Field field : entityClazz.getDeclaredFields()) {

                field.setAccessible(true);
                String firstLetter = field.getName().substring(0, 1).toUpperCase();

                String setMethodName;
                Method setMethod;

                if (field.getDeclaredAnnotation(JsonProperty.class) != null) {
                    String jsonPropertyAnn = field.getDeclaredAnnotation(JsonProperty.class).value();
                    if (jsonProperty.equals(jsonPropertyAnn)) {
                        setMethodName = "set" + firstLetter + field.getName().substring(1);
                        setMethod = entityClazz.getDeclaredMethod(setMethodName, field.getType());
                        setMethod.invoke(bean, args);
                    }
                } else if (field.getName().equals(jsonProperty)) {
                    setMethodName = "set" + firstLetter + field.getName().substring(1);
                    setMethod = entityClazz.getDeclaredMethod(setMethodName, field.getType());
                    setMethod.invoke(bean, args);
                }
            }
            return bean;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
