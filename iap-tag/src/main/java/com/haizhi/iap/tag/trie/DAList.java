package com.haizhi.iap.tag.trie;

import java.io.Serializable;

public class DAList implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -8531832888539845013L;
    private int[] data;
    private int count;

    private int expandFactor;

    public DAList(int size) {
        this(size, 10000);
    }

    public DAList(int size, int factor) {
        data = new int[size];
        count = 0;
        expandFactor = factor;
    }

    public void add(int num) {
        if (count == data.length) {
            int[] newData = new int[data.length + expandFactor];
            System.arraycopy(data, 0, newData, 0, data.length);
            data = newData;
        }
        data[count] = num;
        count++;
    }

    public int getSize() {
        return count;
    }

    public int getExpandFactor() {
        return this.expandFactor;
    }

    public void set(int pos, int num) {
        data[pos] = num;
    }

    public int get(int pos) {
        return data[pos];
    }
}
