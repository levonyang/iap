package com.haizhi.iap.tag.recognizer.meta;


import com.haizhi.iap.tag.trie.ByteCharacterMapping;
import com.haizhi.iap.tag.trie.CharacterMapping;
import com.haizhi.iap.tag.trie.DictionaryException;
import com.haizhi.iap.tag.trie.DoubleArrayTrie;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DATrieDictionary implements Dictionary {

    private DoubleArrayTrie dict;

    public DATrieDictionary() throws DictionaryException {
        this(new ByteCharacterMapping(), 4500000);    // notice: [45000000] 默认申请320+M空间给dict，可以加快启动速度
    }

    public DATrieDictionary(CharacterMapping map) throws DictionaryException {
        this.dict = new DoubleArrayTrie(map, map.getInitSize());
    }

    public DATrieDictionary(CharacterMapping map, int initArraySize) throws DictionaryException {
        this.dict = new DoubleArrayTrie(map, initArraySize);
    }

    public int size() {
        return dict.getNumbers();
    }

    public int get(String key) {
        int[] res = dict.find(key, 0);
        if (res[0] != key.length())
            return -1;
        return res[1];
    }

    public boolean put(String key, Integer value) throws DictionaryException {
        return dict.coverInsert(key, value);
    }

    public int putAll(List<String> keys, List<Integer> values) throws DictionaryException {
        if (keys == null || values == null)
            return -1;
        if (keys.size() != values.size())
            return -1;
        int ret = 0;
        for (int i = 0; i < keys.size(); i++) {
            if (put(keys.get(i), values.get(i))) {
                ret++;
            }
        }
        return ret;
    }

    public boolean remove(String key) {
        int ret = dict.delete(key);
        return ret != -1;
    }

    public int removeAll(List<String> keys) {
        if (keys == null)
            return -1;
        int ret = 0;
        for (String key : keys) {
            if (remove(key)) {
                ret++;
            }
        }
        return ret;
    }

    public List<int[]> query(List<QueryToken> tokens) {
        List<int[]> ret = new ArrayList<int[]>();
        for (int i = 0; i < tokens.size(); i++) {
            int curState = dict.getRoot();
            for (int j = i; j < tokens.size(); j++) {
                int[] res = dict.walkTrie(curState, tokens.get(j).getValue());
                if (res[0] == -1) break;
                curState = res[0];
                if (res[1] != -1) {
                    ret.add(new int[]{i, j + 1, res[1]});
                }
            }
        }
        return ret;
    }

    public void store(OutputStream outputStream) throws DictionaryException {

        if (outputStream == null || !(outputStream instanceof ObjectOutputStream)) {
            throw new IllegalArgumentException("ObjectOutputStream is required.");
        }

        try {
            ((ObjectOutputStream) outputStream).writeObject(getDict());
        } catch (IOException e) {
            throw new DictionaryException(e);
        }
    }

    public void load(InputStream inputStream) throws DictionaryException {
        if (inputStream == null || !(inputStream instanceof ObjectInputStream)) {
            throw new IllegalArgumentException("ObjectInputStream is required.");
        }

        try {
            setDict((DoubleArrayTrie) ((ObjectInputStream) inputStream).readObject());
        } catch (IOException e) {
            throw new DictionaryException(e);
        } catch (ClassNotFoundException e) {
            throw new DictionaryException(e);
        }
    }

    public int getMaximumId() {
        return dict.getMaximumValue();
    }

    public int getArraySize() {
        return dict.getSize();
    }

    public int getEmptySize() {
        return dict.getEmptySize();
    }

    public DoubleArrayTrie getDict() {
        return dict;
    }

    public void setDict(DoubleArrayTrie dict) {
        this.dict = dict;
    }
}
