package com.engine.interfaces.yll2.wuling.archive.service;

import com.customization.yll.common.util.CacheUtil;
import com.engine.interfaces.yll2.wuling.archive.bean.PushWorkflowInfo;
import com.engine.interfaces.yll2.wuling.archive.util.HistoryArchiveCacheUtil;
import org.jetbrains.annotations.NotNull;
import weaver.integration.logging.Logger;
import weaver.integration.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static com.engine.interfaces.yll2.wuling.archive.util.HistoryArchiveCacheUtil.cleanCache;

/**
 * @author yaolilin
 * @desc 历史档案推送任务抽象类, 使用多线程批量的执行历史档案推送任务。
 * <p>使用线程池推送历史档案数据，如果线程池队列已满，则等待队列空闲后继续添加推送任务，如果等待时间超过最大等待时间则放弃剩余的任务推送
 * （表示线程池中的任务可能发生长久的阻塞，导致队列一直是满的状态）。推送成功和推送失败的任务数量会记录到缓存当中，其中推送成功会记录到
 * {@link HistoryArchiveCacheUtil#HISTORY_ARCHIVE_PUSH_FAILED_COUNT_CACHE_KEY} 缓存，失败会记录到
 * {@link HistoryArchiveCacheUtil#HISTORY_ARCHIVE_PUSH_DONE_COUNT_CACHE_KEY} 缓存。
 * @date 2025/1/13
 **/
public abstract class AbstractHistoryArchivePushTask implements Runnable {
    private static final int MAX_WAIT_QUEUE_TIME = 30;
    private final List<PushWorkflowInfo> pushWorkfowList;
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final  AtomicInteger count = new AtomicInteger();
    private final  AtomicInteger failedCount = new AtomicInteger();
    private static final ReentrantLock LOCK = new ReentrantLock();
    private static final Condition TASK_QUEUE_FULL = LOCK.newCondition();
    private static final int TEN_MINUTES = 60 * 60 * 10;

    public AbstractHistoryArchivePushTask(List<PushWorkflowInfo> pushWorkfowList) {
        this.pushWorkfowList = pushWorkfowList;
    }

    /**
     * 使用线程池批量推送历史档案数据，该方法会产生阻塞，直到所有历史档案推送完成。
     */
    @Override
    public void run() {
        ArchivePushTheadPoolService poolService = ArchivePushTheadPoolService.INSTANCE;
        List<Future<Boolean>> results = addPushTasks(poolService);
        for (Future<Boolean> result : results) {
            try {
                result.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                log.error("获取执行结果失败",e);
            }
        }
        log.info("已全部推送完成");
        cleanCache();
        poolService.shutdown();
    }

    @NotNull
    private List<Future<Boolean>> addPushTasks(ArchivePushTheadPoolService poolService) {
        List<Future<Boolean>> results = new ArrayList<>();
        for (PushWorkflowInfo workflow : pushWorkfowList) {
            try {
                if (!submitTaskWhenQueueNotFull(poolService, workflow, results)) {
                    log.error("添加任务到线程池失败，放弃剩余任务");
                    break;
                }
            } catch (RejectedExecutionException e) {
                log.error("线程队列已满，跳过本次推送：" + workflow.getRequestId());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("等待队列空余时中断，不处理本推送：" + workflow.getRequestId());
            }
        }
        return results;
    }

    private boolean submitTaskWhenQueueNotFull(ArchivePushTheadPoolService poolService, PushWorkflowInfo workflow,
                                               List<Future<Boolean>> results)
            throws InterruptedException {
        LOCK.lock();
        try {
            while (poolService.getQueueSize() == ArchivePushTheadPoolService.QUEUE_SIZE) {
                log.info("线程池队列已满，等待队列空余");
                boolean await = TASK_QUEUE_FULL.await(MAX_WAIT_QUEUE_TIME, TimeUnit.MINUTES);
                if (!await) {
                    log.error("等待队列超时");
                    return false;
                }
            }
            results.add(poolService.submitTask(() -> {
                boolean success;
                try {
                    success = pushArchive(workflow);
                } finally {
                    LOCK.lock();
                    try {
                        TASK_QUEUE_FULL.signal();
                    } finally {
                        LOCK.unlock();
                    }
                }
                if (!success) {
                    CacheUtil.putCache(HistoryArchiveCacheUtil.HISTORY_ARCHIVE_PUSH_FAILED_COUNT_CACHE_KEY,
                            failedCount.incrementAndGet(), TEN_MINUTES);
                }
                CacheUtil.putCache(HistoryArchiveCacheUtil.HISTORY_ARCHIVE_PUSH_DONE_COUNT_CACHE_KEY,
                        count.incrementAndGet(), TEN_MINUTES);
                return success;
            }));
            return true;
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * 执行档案推送
     *
     * @param workflow 历史流程
     * @return 是否推送成功
     */
    protected abstract boolean pushArchive(PushWorkflowInfo workflow);
}
