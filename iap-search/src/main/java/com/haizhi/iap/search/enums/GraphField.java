package com.haizhi.iap.search.enums;

/**
 * Created by caochao on 18/8/2.
 */
public enum GraphField {
    EDGES, VERTEXES, _ID, _FROM, _TO, _KEY, LABEL, NAME, _TYPE, VERTICES;

    public String getName() {
        return this.name().toLowerCase();
    }
}
