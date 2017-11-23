package com.lingtuan.firefly.chat.audio;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created on 2016/5/31.
 */
public class ThreadPool {
    private static ThreadPool instance;
    private final int EXECUTOR_THREADS = Runtime.getRuntime().availableProcessors();
    private ExecutorService processorsPools,cachedPools;

    private ThreadPool(){

    }

    public synchronized static ThreadPool getInstance(){
        if (instance==null){
            instance=new ThreadPool();
        }
        return instance;
    }

    public ExecutorService getProcessorsPools(){
        if (processorsPools==null){
            processorsPools = Executors.newFixedThreadPool(EXECUTOR_THREADS);
        }
        return processorsPools;
    }

    public ExecutorService getCachedPools(){
        if (cachedPools==null){
            cachedPools = Executors.newCachedThreadPool();
        }
        return cachedPools;
    }
}
