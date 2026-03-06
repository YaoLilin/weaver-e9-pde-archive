package com.engine.interfaces.yll2.wuling.archive.service;

import cn.hutool.core.thread.NamedThreadFactory;
import lombok.Getter;

import java.util.concurrent.*;

/**
 * @author yaolilin
 * @desc 档案历史流程推送线程池
 * @date 2025/1/13
 **/
@Getter
public enum ArchivePushTheadPoolService {
    /**
     * 线程池实例
     */
    INSTANCE;
    private ThreadPoolExecutor service;
    private static final int CORE_POOL_SIZE = 64;
    private static final int MAX_POOL_SIZE = CORE_POOL_SIZE * 2;
    public static final int QUEUE_SIZE = 7000;

    ArchivePushTheadPoolService() {
        service = createThreadPool();
    }

    public void putTask(Runnable task) {
        if (service.isShutdown()) {
            initPool();
        }
        service.execute(task);
    }

    public Future<Boolean> submitTask(Callable<Boolean> task) {
        if (service.isShutdown()) {
            initPool();
        }
        return service.submit(task);
    }

    public int getTaskCount() {
        return service.getActiveCount() + service.getQueue().size();
    }

    public int getQueueSize() {
        return service.getQueue().size();
    }

    public void shutdown() {
        service.shutdown();
    }

    public void shutdownNow() {
        service.shutdownNow();
    }

    private ThreadPoolExecutor createThreadPool() {
        return new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, 5, TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(QUEUE_SIZE)
                , new NamedThreadFactory(this.getClass().getName(), false)
                , new ThreadPoolExecutor.AbortPolicy());
    }

    private void initPool() {
        synchronized (ArchivePushTheadPoolService.this) {
            if (service.isShutdown()) {
                service = createThreadPool();
            }
        }
    }

}
