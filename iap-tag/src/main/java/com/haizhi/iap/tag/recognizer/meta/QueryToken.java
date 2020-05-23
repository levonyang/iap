package com.haizhi.iap.tag.recognizer.meta;

import java.util.ArrayList;
import java.util.List;

public class QueryToken {

    private int start;
    private int end;
    private String value;

    private Entity selectedEntity;

    private List<Entity> entities = new ArrayList<Entity>();

    public QueryToken() {

    }

    public QueryToken(int start, int end, String value) {
        this.start = start;
        this.end = end;
        this.value = value;
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

    public List<Entity> getEntities() {
        return entities;
    }

    public void setEntities(List<Entity> entities) {
        this.entities = entities;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Entity getSelectedEntity() {
        return selectedEntity;
    }

    public void setSelectedEntity(Entity selectedEntity) {
        this.selectedEntity = selectedEntity;
    }

}
