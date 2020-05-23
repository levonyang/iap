package com.haizhi.iap.tag.recognizer;

import com.google.common.collect.Lists;
import com.haizhi.iap.tag.recognizer.meta.*;
import com.haizhi.iap.tag.recognizer.normalizer.DefaultNormalizer;
import com.haizhi.iap.tag.recognizer.normalizer.Normalizer;
import com.haizhi.iap.tag.recognizer.tokenizer.DefaultTokenizer;
import com.haizhi.iap.tag.recognizer.tokenizer.Tokenizer;
import com.haizhi.iap.tag.recognizer.util.LongWordFirstComparator;
import com.haizhi.iap.tag.trie.ByteCharacterMapping;

import java.util.*;

public class RecognizerManager {

    private static final int DEFAULT_DICTIONARY_SIZE = 10000;

    //private static final RecordType DEFAULT_RECORD_TYPE = new RecordType(0, 0, "default");

    private static final Comparator<Entity> comparator = new LongWordFirstComparator();

    public RecognizerManager() {
        this(DEFAULT_DICTIONARY_SIZE);
    }

    public RecognizerManager(int size) {
        dictionaryManager = new DictionaryManager(new DATrieDictionary(new ByteCharacterMapping(), size));
        normalizer = new DefaultNormalizer();
        tokenizer = new DefaultTokenizer();
    }

    private DictionaryManager dictionaryManager;
    private Normalizer normalizer;
    private Tokenizer tokenizer;

    public <T extends AbstractDictRecord> void buildDictionary(List<T> records) {
        for(T record : records) {
            dictionaryManager.addRecord(record);
        }
    }

    public List<Entity> recognizeEntities(String str) {
        str = normalizer.normalize(str);
        List<QueryToken> tokens = tokenizer.tokenize(str);
        List<Entity> entities = dictionaryManager.recognize(tokens);
        for (Entity e : entities) {
            int pos = e.getStart();
            QueryToken token = tokens.get(pos);
            token.getEntities().add(e);
        }
        List<Entity> ret = Lists.newArrayList();
        for (int i = 0; i < tokens.size(); i++) {
            QueryToken token = tokens.get(i);
            if (!token.getEntities().isEmpty()) {
                List<Entity> ets = token.getEntities();
                Collections.sort(ets, comparator);
                Entity best = ets.get(0);
                int maxLength = best.getEnd() - best.getStart() + 1;
                ret.add(best);
                for (int j = 1; j < ets.size(); j++) {
                    Entity cur = ets.get(j);
                    int length = cur.getEnd() - cur.getStart() + 1;
                    if (length < maxLength) {
                        break;
                    }
                    ret.add(cur);
                }
                i = best.getEnd() - 1;
            }
        }
        return ret;
    }

}
