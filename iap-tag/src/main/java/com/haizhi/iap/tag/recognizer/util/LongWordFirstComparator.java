package com.haizhi.iap.tag.recognizer.util;

import com.haizhi.iap.tag.recognizer.meta.Entity;
import com.haizhi.iap.tag.recognizer.meta.RecordType;

import java.util.Comparator;

public class LongWordFirstComparator implements Comparator<Entity> {

    public int compare(Entity e1, Entity e2) {
        if (e1.getRecord() == null || e2.getRecord() == null)
            return 0;

        RecordType e1Type = e1.getRecord().getType();
        RecordType e2Type = e2.getRecord().getType();

        if (e1.length() > e2.length())
            return -1;
        if (e1.length() < e2.length())
            return 1;

        int e1Weight = e1Type.getPriority();
        int e2Weight = e2Type.getPriority();

        if (e1Weight > e2Weight)
            return -1;
        if (e1Weight < e2Weight)
            return 1;
        return 0;
    }

}
