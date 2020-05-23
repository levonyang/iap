package com.haizhi.iap.tag.recognizer.meta;

import java.io.Serializable;

public abstract class AbstractDictRecord implements Serializable {

    public transient static final String KEY_SEPARATOR = ":";
    private static final long serialVersionUID = 4902898345847189458L;
    protected RecordType type;

    public abstract String getDictKey();

    public RecordType getType() {
        return type;
    }

    public void setType(RecordType type) {
        this.type = type;
    }

    public abstract String getTypeKey();
}
