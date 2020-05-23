package com.haizhi.iap.tag.recognizer.meta;

import com.haizhi.iap.tag.trie.DictionaryException;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DictionaryManager {

    private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    private int recordId;
    private Dictionary dictionary;
    private Map<Integer, Map<String, AbstractDictRecord>> recordMap;

    public DictionaryManager() {
        this.recordMap = new HashMap<Integer, Map<String, AbstractDictRecord>>();
        recordId = 1;
    }

    public DictionaryManager(Dictionary dictionary) {
        this();
        this.dictionary = dictionary;
    }

    public boolean addRecord(AbstractDictRecord record)
            throws DictionaryException {
        rwLock.writeLock().lock();
        try {
            String dictKey = record.getDictKey();

            int val = dictionary.get(dictKey);

            if (val == -1) {
                val = recordId;
                recordId = (recordId + 1) % dictionary.getMaximumId();
                int lastId = val;
                while (recordMap.containsKey(val)) {
                    val = recordId;
                    recordId = (recordId + 1) % dictionary.getMaximumId();
                    if (lastId == val) {
                        throw new DictionaryException("内部id已用光", null);
                    }
                }
                Map<String, AbstractDictRecord> data = new HashMap<String, AbstractDictRecord>();
                recordMap.put(val, data);
                dictionary.put(dictKey, val);
            }

            Map<String, AbstractDictRecord> has = recordMap.get(val);
            has.put(record.getTypeKey(), record);
            return true;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public int addAllRecords(Collection<? extends AbstractDictRecord> records)
            throws DictionaryException {
        if (records == null)
            return -1;
        int ret = 0;
        for (AbstractDictRecord record : records) {
            if (addRecord(record)) {
                ret++;
            }
        }
        return ret;
    }

    public boolean deleteRecord(AbstractDictRecord record) {
        rwLock.writeLock().lock();
        try {
            int val = dictionary.get(record.getDictKey());
            if (val == -1) {
                return false;
            }
            Map<String, AbstractDictRecord> data = recordMap.get(val);
            if (data != null) {
                boolean isContain = false;
                if (data.containsKey(record.getTypeKey())) {
                    data.remove(record.getTypeKey());
                    isContain = true;
                }
                if (data.isEmpty()) {
                    dictionary.remove(record.getDictKey());
                    recordMap.remove(val);
                }
                return isContain;
            }
            return false;
        } finally {
            rwLock.writeLock().unlock();
        }

    }

    public int deleteAllRecords(List<? extends AbstractDictRecord> records) {
        if (records == null)
            return -1;
        int ret = 0;
        for (AbstractDictRecord record : records) {
            if (deleteRecord(record)) {
                ret++;
            }
        }
        return ret;
    }

    public List<Entity> recognize(List<QueryToken> tokens) {
        rwLock.readLock().lock();
        try {
            List<Entity> ret = new ArrayList<Entity>();
            List<int[]> getWords = dictionary.query(tokens);
            for (int i = 0; i < getWords.size(); i++) {
                int[] w = getWords.get(i);
                Map<String, AbstractDictRecord> rec = recordMap.get(w[2]);
                if (rec != null) {
                    for (Entry<String, AbstractDictRecord> entry : rec
                            .entrySet()) {
                        Entity et = new Entity(w[0], w[1], entry.getValue());
                        ret.add(et);
                    }
                }
            }
            return ret;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public AbstractDictRecord getRecord(String dictKey, String typeKey) {
        rwLock.readLock().lock();
        try {
            int val = dictionary.get(dictKey);
            if (val == -1)
                return null;
            Map<String, AbstractDictRecord> lists = this.recordMap.get(val);
            if (null == lists)
                return null;
            AbstractDictRecord ret = lists.get(typeKey);

            return ret;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public Map<String, AbstractDictRecord> getRecord(String dictKey) {
        rwLock.readLock().lock();
        try {
            int val = dictionary.get(dictKey);
            if (val == -1)
                return null;
            Map<String, AbstractDictRecord> lists = this.recordMap.get(val);
            if (null == lists)
                return null;
            Map<String, AbstractDictRecord> ret = new HashMap<String, AbstractDictRecord>();
            for (Entry<String, AbstractDictRecord> et : lists.entrySet()) {
                ret.put(et.getKey(), et.getValue());
            }
            return ret;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public int getRecordId() {
        return this.recordId;
    }

    public Map<Integer, Map<String, AbstractDictRecord>> getRecords() {
        return this.recordMap;
    }

}
