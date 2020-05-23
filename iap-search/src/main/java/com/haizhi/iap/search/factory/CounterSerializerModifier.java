package com.haizhi.iap.search.factory;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.haizhi.iap.search.controller.model2.Counter;

import java.util.List;

/**
 * Created by chenbo on 2017/11/8.
 */
public class CounterSerializerModifier extends BeanSerializerModifier {
    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
        // 循环所有的beanPropertyWriter
        for (int i = 0; i < beanProperties.size(); i++) {
            BeanPropertyWriter writer = beanProperties.get(i);
            // 判断字段的类型，如果是array，list，set则注册nullSerializer
            if (isCounter(writer)) {
                //给writer注册一个自己的nullSerializer
                writer.assignSerializer(new CounterSerializer());
            }
        }
        return beanProperties;
    }

    // 判断是什么类型
    protected boolean isCounter(BeanPropertyWriter writer) {
        JavaType javaType = writer.getType();
        return javaType.isTypeOrSubTypeOf(Counter.class);

    }
}
