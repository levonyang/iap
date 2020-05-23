package com.haizhi.iap.tag.recognizer;


import com.haizhi.iap.tag.recognizer.meta.AbstractDictRecord;
import com.haizhi.iap.tag.recognizer.meta.Entity;
import com.haizhi.iap.tag.recognizer.meta.PatternRecord;
import com.haizhi.iap.tag.recognizer.meta.RecordType;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TestRecognizerManager {

   /* @Test
    public void testRecognizer() {
        Map<Integer, List<String>> data = new HashMap<Integer, List<String>>();
        List<String> a1 = new LinkedList<String>();
        a1.add("望京");
        a1.add("北京");
        a1.add("朝阳");
        a1.add("wocao");
        data.put(1, a1);
        PatternRecord one = new PatternRecord(DEFAULT_RECORD_TYPE, tag, dict.getKey());

        List<String> b1 = new LinkedList<String>();
        b1.add("回龙观");
        b1.add("昌平区");
        b1.add("昌平");
        data.put(2, b1);

        List<String> c1 = new LinkedList<String>();
        c1.add("西直门");
        c1.add("昌平");
        c1.add("北京");
        data.put(3, c1);

        RecognizerManager rm = new RecognizerManager();
        rm.buildDictionary(data);

        List<Entity> all = rm.recognizeEntities("北京朝阳区望京融科大厦B层");
        Assert.assertEquals(3, all.size());
        for (Entity one : all) {
            System.out.println(((PatternRecord)one.getRecord()).getName());
        }
    }*/

    private static final RecordType DEFAULT_RECORD_TYPE = new RecordType(0, 0, "default");

    @Test
    public void mtest() {
        List<AbstractDictRecord> records = new ArrayList<>();
        PatternRecord patternRecord = null;

        patternRecord = new PatternRecord(DEFAULT_RECORD_TYPE, "望京", 1);
        records.add(patternRecord);
        patternRecord = new PatternRecord(DEFAULT_RECORD_TYPE, "北京", 1);
        records.add(patternRecord);
        patternRecord = new PatternRecord(DEFAULT_RECORD_TYPE, "朝阳", 1);
        records.add(patternRecord);
        patternRecord = new PatternRecord(DEFAULT_RECORD_TYPE, "wocao", 1);
        records.add(patternRecord);
        patternRecord = new PatternRecord(DEFAULT_RECORD_TYPE, "北京", 2);
        records.add(patternRecord);

        RecognizerManager rm = new RecognizerManager();
        rm.buildDictionary(records);

        List<Entity> all = rm.recognizeEntities("北京朝阳区望京融科大厦B层");
        //Assert.assertEquals(3, all.size());
        for (Entity one : all) {
            System.out.print(((PatternRecord)one.getRecord()).getName());
            System.out.println((int)((PatternRecord)one.getRecord()).getData());
        }
    }
}
