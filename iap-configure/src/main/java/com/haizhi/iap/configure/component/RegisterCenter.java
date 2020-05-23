package com.haizhi.iap.configure.component;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.concurrent.ConcurrentMap;

/**
 * Created by chenbo on 2017/10/10.
 */
@Data
@AllArgsConstructor
public class RegisterCenter {

    private ConcurrentMap<Long, Thread> pool;
    private ConcurrentMap<Long, Boolean> signalMap;

    private static final Integer MAX_POOL_SIZE = 10;

    public synchronized void register(Long id, Thread thread) {
        if (!isFull()) {
            this.pool.put(id, thread);
            this.signalMap.put(id, false);
        }
    }

    public synchronized boolean isShut(Long id) {
        if (this.signalMap.get(id) == null) {
            return false;
        } else {
            return this.signalMap.get(id);
        }
    }

    public synchronized void shut(Long id){
        this.signalMap.put(id, true);
    }

    public synchronized void remove(Long id) {
        if (this.getPool().get(id) != null) {
            this.pool.remove(id);
        }
    }

    public synchronized Integer getSize() {
        return this.pool.size();
    }

    public synchronized boolean isFull() {
        return this.pool.size() >= MAX_POOL_SIZE;
    }
}
