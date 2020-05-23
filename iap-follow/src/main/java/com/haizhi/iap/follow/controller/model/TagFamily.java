package com.haizhi.iap.follow.controller.model;

import com.haizhi.iap.follow.model.Tag;
import lombok.Data;

import java.text.Collator;
import java.util.List;
import java.util.Locale;

/**
 * Created by chenbo on 2017/12/15.
 */
@Data
public class TagFamily implements Comparable {

    Tag parent;

    List<Tag> children;

    @Override
    public int compareTo(Object o) {
        if (o instanceof TagFamily) {
            return Collator.getInstance(Locale.CHINESE).compare(this.parent.getName(),
                    ((TagFamily) o).getParent().getName());
        }
        return 1;
    }
}
