package com.engine.interfaces.yll2.wuling.archive.service;

import com.customization.yll.common.IntegrationLog;
import com.customization.yll.common.util.CacheUtil;
import com.customization.yll.wuling.archive.api.ArchiveDataPushManager;
import com.customization.yll.wuling.archive.data.ArchiveDataManager;
import com.customization.yll.wuling.archive.file.PackArchiveDataManager;
import com.customization.yll.wuling.archive.service.ArchiveDataPushTask;
import com.customization.yll.wuling.archive.service.PushResult;
import com.engine.interfaces.yll2.wuling.archive.bean.PushWorkflowInfo;
import com.engine.interfaces.yll2.wuling.archive.util.HistoryArchiveCacheUtil;

import java.util.List;

/**
 * @author 姚礼林
 * @desc 历史档案推送任务
 * @date 2025/11/3
 **/
public class HistoryArchivePushTask extends AbstractHistoryArchivePushTask {
    private final IntegrationLog log = new IntegrationLog(HistoryArchivePushTask.class);

    public HistoryArchivePushTask(List<PushWorkflowInfo> pushWorkfowList) {
        super(pushWorkfowList);
    }

    @Override
    protected boolean pushArchive(PushWorkflowInfo workflow) {
        if (CacheUtil.getCache(HistoryArchiveCacheUtil.HISTORY_ARCHIVE_PUSHING_FLAG_CACHE_KEY) == null) {
            log.info("缓存中没有推送标识，停止推送");
            return false;
        }
        PackArchiveDataManager packManager = new PackArchiveDataManager(new ArchiveDataManager(workflow.getRequestId(),
                workflow.getConfigId()), workflow.getRequestId());
        ArchiveDataPushTask task = new ArchiveDataPushTask(workflow.getRequestId(),
                workflow.getConfigId(), true, true, packManager,
                new ArchiveDataPushManager());
        PushResult result = task.push();
        return result.isSuccess();
    }
}
