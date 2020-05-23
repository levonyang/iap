package com.haizhi.iap.search.factory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.haizhi.iap.search.controller.model2.Counter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by chenbo on 2017/11/8.
 */
@Slf4j
public class CounterSerializer extends JsonSerializer<Object> {

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        //Counter的子类
        Class clazz = value.getClass();
        if (Counter.class.isInstance(value)) {
            Counter father = (Counter) value;

            if(father.getTotalCount() == null){
                Long totalCount = 0L;

                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);
                    if (field.getType() != null && field.getType().getSuperclass()!= null
                            && field.getType().getSuperclass().equals(Counter.class)) {
                        String fieldName = field.getName();
                        try {
                            Method getMethod = clazz.getDeclaredMethod("get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1));
                            Counter child = (Counter) getMethod.invoke(value);
                            if(child != null && child.getTotalCount() != null){
                                totalCount += child.getTotalCount();
                            }
                        } catch (Exception e) {
                            log.error("{}", e);
                        }
                    }
                }

                father.setTotalCount(totalCount);
            }
        }
        gen.writeObject(value);
    }
}
