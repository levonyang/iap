package com.haizhi.iap.tag.recognizer.meta;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Entity {
    private int start;
    private int end;
    private AbstractDictRecord record;

    public Entity(int start, int end, AbstractDictRecord record) {
        this.start = start;
        this.end = end;
        this.record = record;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractDictRecord> T getRecord() {
        return (T) record;
    }

    public void setRecord(AbstractDictRecord record) {
        this.record = record;
    }

    public int length() {
        return end - start;
    }

    public RecordType getType() {
        return record.getType();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(record.getTypeKey()).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Entity)) {
            return false;
        }
        return this.record.getTypeKey().equals(((Entity) obj).getRecord().getTypeKey());
    }
}
