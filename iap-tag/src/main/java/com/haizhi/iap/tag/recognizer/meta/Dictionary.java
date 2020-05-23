package com.haizhi.iap.tag.recognizer.meta;

import com.haizhi.iap.tag.trie.DictionaryException;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface Dictionary {

    public static final int ID_NOT_FOUND = -1;

    /**
     * 获取字典当前纪录数
     *
     * @return
     */
    int size();

    /**
     * 查找key对应的value值。
     *
     * @param key 词。
     * @return 若不存在，返回-1。
     */
    int get(String key);

    /**
     * 新增纪录
     *
     * @param key   词
     * @param value
     * @return
     */
    boolean put(String key, Integer value) throws DictionaryException, DictionaryException;

    /**
     * 批量添加
     *
     * @param keys,   词
     * @param values, 词对应的value, 注意keys.size() == values.size()
     * @return 成功加入的词数
     */
    int putAll(List<String> keys, List<Integer> values) throws DictionaryException;

    /**
     * 删除一个词
     *
     * @param key
     * @return
     */
    boolean remove(String key);

    /**
     * 删除列表中的所有词
     *
     * @param keys 词列表
     * @return 成功删除条数
     */
    int removeAll(List<String> keys);

    /**
     * 查询tokens中包含的词
     *
     * @param tokens
     * @return 0->起点，1->终点，2->value
     */
    List<int[]> query(List<QueryToken> tokens);

    /**
     * 存储字典
     *
     * @param outputStream 存储输出
     */
    void store(OutputStream outputStream) throws DictionaryException;

    /**
     * 加载字典
     *
     * @param inputStream 加载读取
     */
    void load(InputStream inputStream) throws DictionaryException;

    /**
     * 词典可以存放的最大value
     *
     * @return 最大value
     */
    int getMaximumId();
}
