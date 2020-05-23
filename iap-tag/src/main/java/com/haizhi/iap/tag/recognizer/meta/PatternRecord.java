package com.haizhi.iap.tag.recognizer.meta;


import com.haizhi.iap.tag.recognizer.util.NormalizeUtil;

public class PatternRecord extends AbstractDictRecord {

    private static final long serialVersionUID = -7347285473431103553L;

    private String name;

    private Object data;

    private String dictKey;

    public PatternRecord(RecordType recordType, String name, Object data) {
        setType(recordType);
        this.name = name;
        this.data = data;
        this.dictKey = NormalizeUtil.normalize(name);
    }

    @Override
    public String getDictKey() {
        return dictKey;
    }

    public void setDictKey(String dictKey) {
        this.dictKey = dictKey;
    }

    @Override
    public String getTypeKey() {
        return type.getRecordId() + KEY_SEPARATOR + name;
    }

    @Override
    public RecordType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @SuppressWarnings("unchecked")
    public <T> T getData() {
        return (T) data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
