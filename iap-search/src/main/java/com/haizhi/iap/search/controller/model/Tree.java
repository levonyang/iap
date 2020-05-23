package com.haizhi.iap.search.controller.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by thomas on 18/3/21.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Tree<T>
{
    /**
     * 当前结点的所有属性
     */
    private Map<String, Object> properties;

    /**
     * 当前结点与父结点之间的关系(如：invest, officer等等)
     * 可以是一个List或Map，视需要而定
     */
    private T relations;

    /**
     * 当前结点的所有孩子结点
     */
    private List<Tree<T>> children;

    public Tree(Map<String, Object> properties) {
        this.properties = new HashMap<>(properties);
    }

    public Tree(Map<String, Object> properties, T relations) {
        this.properties = new HashMap<>(properties);
        this.relations = relations;
    }
}
