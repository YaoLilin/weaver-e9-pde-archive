package com.customization.yll.wuling.archive.service;

import cn.hutool.core.thread.NamedThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 处理档案推送任务的线程池
 * @author yll
 */
public enum ArchiveDataPushTaskService {
    /**
     * 线程池实例
     */
    INSTANCE;
    private ExecutorService service;

    ArchiveDataPushTaskService() {
        service = createThreadPool();
    }

    public void putTask(DataPushTask task) {
        if (service.isShutdown()) {
            synchronized (ArchiveDataPushTaskService.this) {
                if (service.isShutdown()) {
                    service = createThreadPool();
                }
            }
        }
        service.execute(task::push);
    }

    private ExecutorService createThreadPool() {
        return new ThreadPoolExecutor(64, 128, 10, TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(700),new NamedThreadFactory("archive-push",false)
        ,new ThreadPoolExecutor.CallerRunsPolicy());
    }

    private void shutdown() {
        service.shutdown();
    }
}
