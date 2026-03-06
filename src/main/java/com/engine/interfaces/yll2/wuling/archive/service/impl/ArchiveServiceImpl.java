package com.engine.interfaces.yll2.wuling.archive.service.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.customization.yll.common.exception.SqlExecuteException;
import com.customization.yll.common.util.CacheUtil;
import com.customization.yll.common.util.SqlUtil;
import com.customization.yll.wuling.archive.api.ArchiveDataPushManager;
import com.customization.yll.wuling.archive.constants.ArchiveResultModeInfo;
import com.customization.yll.wuling.archive.data.ArchiveDataManager;
import com.customization.yll.wuling.archive.file.PackArchiveDataManager;
import com.customization.yll.wuling.archive.service.ArchiveDataPushTask;
import com.engine.core.impl.Service;
import com.engine.interfaces.yll2.wuling.archive.bean.ArchiveResult;
import com.engine.interfaces.yll2.wuling.archive.bean.PushWorkflowInfo;
import com.engine.interfaces.yll2.wuling.archive.model.dto.HistoryArchivePushResult;
import com.engine.interfaces.yll2.wuling.archive.model.vo.ArchiveHistoryPushStatus;
import com.engine.interfaces.yll2.wuling.archive.service.*;
import lombok.Setter;
import weaver.conn.RecordSet;
import weaver.integration.logging.Logger;
import weaver.integration.logging.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.customization.yll.wuling.archive.constants.ArchiveResultModeInfo.TABLE_NAME;
import static com.engine.interfaces.yll2.wuling.archive.util.HistoryArchiveCacheUtil.initCache;

/**
 * 档案集成接口业务
 *
 * @author yaolilin
 * @date 2024-08-29
 */
@Setter
public class ArchiveServiceImpl extends Service implements ArchiveService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private static final String HISTORY_ARCHIVE_PUSHING_FLAG_CACHE_KEY = "historyArchivePushing";
    private static final String HISTORY_ARCHIVE_PUSH_DONE_COUNT_CACHE_KEY = "historyArchivePushDoneCount";
    private static final String HISTORY_ARCHIVE_PUSH_FAILED_COUNT_CACHE_KEY = "historyArchivePushFailedCount";
    private static final String HISTORY_ARCHIVE_PUSH_TOTAL_COUNT_CACHE_KEY = "historyArchivePushTotalCount";
    private final ArchiveStatusApiResultAnalysisHelper apiResultAnalysisHelper;
    private ArchiveHistoryWorkflowService archiveHistoryWorkflowService = new ArchiveHistoryWorkflowServiceImpl();

    public ArchiveServiceImpl(ArchiveStatusApiResultAnalysisHelper apiResultAnalysisHelper) {
        this.apiResultAnalysisHelper = apiResultAnalysisHelper;
    }

    public ArchiveServiceImpl() {
        apiResultAnalysisHelper = new ArchiveStatusApiResultAnalysisHelper();
    }

    /**
     * 更新归档状态
     *
     * @param params 档案系统推送过来的数据
     */
    @Override
    public void recordStatus(JSONObject params) {
        RecordSet recordSet = new RecordSet();

        List<ArchiveResult> resultList = apiResultAnalysisHelper.analysis(params.toJSONString());
        for (ArchiveResult result : resultList) {
            updateStatus(result, params.toJSONString(), recordSet);
        }
    }

    /**
     * 推送档案信息
     *
     * @param configId  配置建模的数据id
     * @param requestId 流程请求id
     * @param isHistoryWorkflow 是否历史流程
     */
    @Override
    public void push(int configId, int requestId, boolean isHistoryWorkflow) {
        PackArchiveDataManager packManager = new PackArchiveDataManager(new ArchiveDataManager(requestId, configId),
                requestId);
        ArchiveDataPushTask task = new ArchiveDataPushTask(requestId, configId, true,
                isHistoryWorkflow, packManager, new ArchiveDataPushManager());
        task.push();
    }

    /**
     * 推送历史流程数据到档案系统
     * @param workflowIds 需要推送的流程id集合
     */
    @Override
    public HistoryArchivePushResult pushHistoryWorkflow(List<Integer> workflowIds) {
        if (!CacheUtil.isRedis()) {
            log.info("当前环境没有集成 redis");
        }
        if (CacheUtil.getCache(HISTORY_ARCHIVE_PUSHING_FLAG_CACHE_KEY) != null) {
            return new HistoryArchivePushResult("正在推送历史流程，请稍后再试", false, false);
        }
        List<PushWorkflowInfo> pushWorkfowList = archiveHistoryWorkflowService.getPushHistoryWorkflowList(workflowIds);
        if (pushWorkfowList.isEmpty()) {
            return new HistoryArchivePushResult("没有需要推送的历史流程", true, false);
        }
        log.info("推送历史流程总数：" + pushWorkfowList.size());
        initCache(pushWorkfowList.size());
        beginBatchPush(pushWorkfowList);
        return new HistoryArchivePushResult("已开始推送历史流程档案，请耐心等待", true, true);
    }

    private void beginBatchPush(List<PushWorkflowInfo> pushWorkfowList) {
        log.info("开始历史流程批量推送");
        ArchivePushTheadPoolService poolService = ArchivePushTheadPoolService.INSTANCE;
        AbstractHistoryArchivePushTask task = new HistoryArchivePushTask(pushWorkfowList);
        poolService.putTask(task);
    }

    @Override
    public void stopHistoryPush() {
        ArchivePushTheadPoolService poolService = ArchivePushTheadPoolService.INSTANCE;
        // 清空档案推送任务队列
        poolService.getService().getQueue().clear();
        cleanCache();
        poolService.shutdown();
        log.info("关闭线程池");
        try {
            if (!poolService.getService().awaitTermination(3, TimeUnit.MINUTES)) {
                log.info("关闭线程池超时，强制关闭线程池");
                poolService.shutdownNow();
            }
        } catch (InterruptedException e) {
            poolService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public ArchiveHistoryPushStatus getHistoryPushStatus() {
        ArchiveHistoryPushStatus status = new ArchiveHistoryPushStatus();
        if(CacheUtil.getCache(HISTORY_ARCHIVE_PUSHING_FLAG_CACHE_KEY) == null){
            status.setPushing(false);
            return status;
        }
        status.setPushing(true);
        status.setTotalCount(objectToInt(CacheUtil.getCache(HISTORY_ARCHIVE_PUSH_TOTAL_COUNT_CACHE_KEY,
                0)));
        status.setDoneCount(objectToInt(CacheUtil.getCache(HISTORY_ARCHIVE_PUSH_DONE_COUNT_CACHE_KEY,0)));
        status.setFailedCount(objectToInt(CacheUtil.getCache(HISTORY_ARCHIVE_PUSH_FAILED_COUNT_CACHE_KEY,
                0)));
        return status;
    }


    private void updateStatus(ArchiveResult archiveResult,String apiBody, RecordSet recordSet) {
        String nowDate = DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
        Map<String, Object> data = new HashMap<>(10);
        data.put(ArchiveResultModeInfo.STATUS_FIELD_NAME, archiveResult.getStatus());
        data.put(ArchiveResultModeInfo.MSG_FIELD_NAME, archiveResult.getMessage());
        data.put(ArchiveResultModeInfo.FEEDBACK_DATE_FIELD_NAME, nowDate);
        data.put(ArchiveResultModeInfo.CALLBACK_RESULT, apiBody);

        List<Object> params = new ArrayList<>(data.values());
        params.add(archiveResult.getSourceId());
        if (!recordSet.executeUpdate("update " + TABLE_NAME + " set " +
                SqlUtil.buildUpdateSql(new ArrayList<>(data.keySet())) + " where "
                + ArchiveResultModeInfo.SOURCE_ID + "=?", params)) {
            log.error("更新归档状态失败，sql执行错误");
            throw new SqlExecuteException("更新归档状态失败，sql执行错误");
        }
    }

    private void cleanCache() {
        CacheUtil.deleteCache(HISTORY_ARCHIVE_PUSHING_FLAG_CACHE_KEY);
        CacheUtil.deleteCache(HISTORY_ARCHIVE_PUSH_DONE_COUNT_CACHE_KEY);
        CacheUtil.deleteCache(HISTORY_ARCHIVE_PUSH_FAILED_COUNT_CACHE_KEY);
    }

    private int objectToInt(Object object) {
        return object == null ? 0 : Integer.parseInt(object + "");
    }
}
