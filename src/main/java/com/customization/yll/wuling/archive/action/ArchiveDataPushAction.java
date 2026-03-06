package com.customization.yll.wuling.archive.action;

import com.customization.yll.common.util.WorkflowActionUtil;
import com.customization.yll.common.workflow.WorkflowActionExceptionHandle;
import com.customization.yll.wuling.archive.api.ArchiveDataPushManager;
import com.customization.yll.wuling.archive.config.ArchiveConfigModeService;
import com.customization.yll.wuling.archive.config.ArchiveConfigModeServiceImpl;
import com.customization.yll.wuling.archive.data.ArchiveDataManager;
import com.customization.yll.wuling.archive.file.PackArchiveDataManager;
import com.customization.yll.wuling.archive.service.ArchiveDataPushTask;
import com.customization.yll.wuling.archive.service.ArchiveDataPushTaskService;
import com.customization.yll.wuling.archive.service.DataPushTask;
import com.customization.yll.wuling.archive.service.PushResult;
import lombok.Setter;
import weaver.conn.RecordSet;
import weaver.integration.logging.Logger;
import weaver.integration.logging.LoggerFactory;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

import java.util.Optional;

/**
 * 流程调用该Action，封装档案信息包推送到档案系统，这个过程是异步的，不影响流程流转
 * @author yaolilin
 */
@Setter
public class ArchiveDataPushAction implements Action {
    /**
     * 是否异步执行，异步为1，同步为0
     */
    private String async = "1";
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private DataPushTask pushTask;
    private ArchiveConfigModeService archiveConfigModeService;

    public ArchiveDataPushAction(DataPushTask pushTask, ArchiveConfigModeService archiveConfigModeService) {
        this.pushTask = pushTask;
        this.archiveConfigModeService = archiveConfigModeService;
    }

    public ArchiveDataPushAction() {
    }

    @Override
    public String execute(RequestInfo requestInfo) {
        try {
            log.info("执行档案推送，请求id：" + requestInfo.getRequestid());
            Optional<Integer> configId = getConfigId(Integer.parseInt(requestInfo.getWorkflowid()));
            if (!configId.isPresent()) {
                log.error("找不到对应的档案推送配置，请确认档案配置中是否有配置该流程");
                WorkflowActionUtil.putUserFailedMsg("找不到对应的档案推送配置，请确认档案配置中是否有配置该流程",
                        requestInfo.getRequestManager(), this.getClass());
                return FAILURE_AND_CONTINUE;
            }
            int requestId = Integer.parseInt(requestInfo.getRequestid());
            if (this.pushTask == null) {
                PackArchiveDataManager packManager = new PackArchiveDataManager(new ArchiveDataManager(requestId,
                        configId.get()), requestId);
                pushTask = new ArchiveDataPushTask(requestId,
                        configId.get(), "1".equals(async), false, packManager,
                        new ArchiveDataPushManager());
            }
            if ("1".equals(async)) {
                ArchiveDataPushTaskService service  = ArchiveDataPushTaskService.INSTANCE;
                service.putTask(pushTask);
            }else {
                PushResult result = pushTask.push();
                if (!result.isSuccess()) {
                    WorkflowActionUtil.putUserFailedMsg(result.getMessage(),requestInfo.getRequestManager(),
                            this.getClass());
                    return FAILURE_AND_CONTINUE;
                }
            }
        } catch (Exception e) {
            WorkflowActionExceptionHandle.handle(requestInfo,e, this.getClass());
            return FAILURE_AND_CONTINUE;
        }
        return SUCCESS;
    }

    private Optional<Integer> getConfigId(int workflowId) {
        if (this.archiveConfigModeService == null) {
            this.archiveConfigModeService = new ArchiveConfigModeServiceImpl(new RecordSet());
        }
        return archiveConfigModeService.getConfigId(workflowId);
    }
}
