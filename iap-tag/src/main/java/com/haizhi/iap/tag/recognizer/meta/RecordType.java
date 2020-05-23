package com.haizhi.iap.tag.recognizer.meta;


public class RecordType {

    private int priority;
    private int recordId;
    private String recordName;

    public RecordType(int priority, int recordId, String recordName) {
        this.priority = priority;
        this.recordId = recordId;
        this.recordName = recordName;
    }

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public String getRecordName() {
        return recordName;
    }

    public void setRecordName(String recordName) {
        this.recordName = recordName;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

}
