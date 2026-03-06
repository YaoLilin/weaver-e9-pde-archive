package com.customization.yll.wuling.archive.service;

import cn.hutool.core.util.StrUtil;
import com.customization.yll.common.exception.ConfigModeDataNotFoundException;
import com.customization.yll.common.exception.FieldValueEmptyException;
import com.customization.yll.common.mode.conf.ParamConfManager;
import com.customization.yll.common.util.ModeUtil;
import com.customization.yll.common.util.SqlUtil;
import com.customization.yll.common.util.WorkflowUtil;
import com.customization.yll.common.workflow.WorkflowFieldValueManager;
import com.customization.yll.wuling.archive.api.ApiResult;
import com.customization.yll.wuling.archive.api.ArchiveDataPushManager;
import com.customization.yll.wuling.archive.config.ArchiveConfig;
import com.customization.yll.wuling.archive.config.ConfigurationModeDataManager;
import com.customization.yll.wuling.archive.constants.ArchiveStatus;
import com.customization.yll.wuling.archive.entity.FileMetadataConfEntity;
import com.customization.yll.wuling.archive.exception.UploadPackageException;
import com.customization.yll.wuling.archive.file.PackArchiveDataManager;
import com.customization.yll.wuling.archive.util.ObjectConvertUtil;
import org.jetbrains.annotations.NotNull;
import weaver.conn.RecordSet;
import weaver.integration.logging.Logger;
import weaver.integration.logging.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.customization.yll.wuling.archive.constants.ArchiveResultModeInfo.*;

/**
 * 推送档案信息包任务
 *
 * @author yaolilin
 */
public class ArchiveDataPushTask implements DataPushTask {
    private final Integer requestId;
    private final Integer configId;
    private final RecordSet recordSet = new RecordSet();
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final boolean needWorkflowFinished;
    private final boolean isHistoryWorkflow;
    private int waitCount = 0;
    private final PackArchiveDataManager packManager;
    private final ArchiveDataPushManager archiveDataPushManager;

    public ArchiveDataPushTask(Integer requestId, Integer configId, boolean needWorkflowFinished,
                               boolean isHistoryWorkflow, PackArchiveDataManager packArchiveDataManager,
                               ArchiveDataPushManager archiveDataPushManager) {
        this.requestId = requestId;
        this.configId = configId;
        this.needWorkflowFinished = needWorkflowFinished;
        this.isHistoryWorkflow = isHistoryWorkflow;
        this.packManager = packArchiveDataManager;
        this.archiveDataPushManager = archiveDataPushManager;
    }

    @Override
    public PushResult push() {
        long startTime = System.currentTimeMillis();
        try {
            log.info("开始推送档案，请求id：" + requestId);
            if (needWorkflowFinished && !isWorkflowFinished()) {
                writeResultToMode(ArchiveStatus.FAILED,false, "推送失败，流程未归档",
                        "", "", "");
                return new PushResult(false, "推送失败，流程未归档");
            }
            if (!isNeedArchive()) {
                writeResultToMode(ArchiveStatus.NOT_NEED_ARCHIVE,false, "没有在归档范围，无需归档",
                        "", "", "");
                return new PushResult(true, "没有在归档范围，无需归档");
            }
            File packageFile = new File(packManager.pack());
            return pushArchieData(packageFile);
        } catch (Exception e) {
            log.error("推送档案失败", e);
            writeResultToModeWithException(e);
            if (e.getCause() instanceof FieldValueEmptyException) {
                return new PushResult(false, "档案推送失败：" + e.getCause().getMessage());
            }
            return new PushResult(false, "档案推送失败，请联系管理员处理");
        } finally {
            deleteTempFiles(packManager);
            log.info("推送档案信息包任务耗时：" + (System.currentTimeMillis() - startTime) + "ms");
        }
    }

    private static void deleteTempFiles(PackArchiveDataManager packManager) {
        String deleteTempFiles = ArchiveConfig.getDeleteTempFiles();
        boolean enableDeleteTempFiles = "1".equals(deleteTempFiles) || StrUtil.isEmpty(deleteTempFiles);
        if (enableDeleteTempFiles && packManager != null) {
            packManager.deleteTempFiles();
        }
    }

    /**
     * 通过调用档案系统归档范围查询接口，判断是否需要推送到档案系统
     * @return 是否需要推送档案系统
     */
    private boolean isNeedArchive() {
        if (!ArchiveConfig.enableArchivingScope()) {
            return true;
        }
        ConfigurationModeDataManager confModeDataManager = new ConfigurationModeDataManager(configId);
        WorkflowFieldValueManager fieldValueManager = new WorkflowFieldValueManager(requestId);
        ParamConfManager paramConfManager = new ParamConfManager(fieldValueManager);
        ArchiveScopeValidator validator = new ArchiveScopeValidator();
        List<FileMetadataConfEntity> fileMetadataConfDetail = confModeDataManager.getFileMetadataConfDetail();
        for (FileMetadataConfEntity entity : fileMetadataConfDetail) {
            if ("题名".equals(entity.getName())) {
                String paramValue = paramConfManager
                        .getParamValue(ObjectConvertUtil.convertToParamConfigurationEntity(entity));
                ValidateResult result = validator.validate(paramValue);
                return result.isInScope();
            }
        }
        throw new ConfigModeDataNotFoundException("配置建模的文件实体元数据中未找到【题名】元数据配置，无法校验是否需要归档");
    }

    @NotNull
    private PushResult pushArchieData(File packageFile) throws FileNotFoundException, UploadPackageException {
        ApiResult apiResult = this.archiveDataPushManager.push(requestId, packageFile.getAbsolutePath());
        int status = apiResult.isSuccess() ? ArchiveStatus.WAITING : ArchiveStatus.FAILED;
        writeResultToMode(status,apiResult.isSuccess(), apiResult.isSuccess() ? "推送成功" : "推送档案信息包失败",
                apiResult.getSourceId(), apiResult.getParams(), apiResult.getResult());
        if (apiResult.isSuccess()) {
            log.info("推送成功");
            return new PushResult(true, "推送成功");
        }
        log.error("推送失败");
        return new PushResult(false, "推送失败，调用接口返回错误，请联系管理员处理");
    }

    private boolean isWorkflowFinished() {
        if (waitCount > 3) {
            return false;
        }
        if (!WorkflowUtil.isWorkflowFinished(requestId, recordSet)) {
            waitCount++;
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                log.error("线程休眠失败", e);
                Thread.currentThread().interrupt();
            }
            return isWorkflowFinished();
        }
        return true;
    }

    private void writeResultToModeWithException(Exception e) {
        if (e.getCause() != null) {
            writeResultToMode(ArchiveStatus.FAILED,false,
                    "程序发生异常：" + e.getMessage() + "," + e.getCause().getMessage(),
                    null, "", "");
        } else {
            writeResultToMode(ArchiveStatus.FAILED,false, "程序发生异常：" + e.getMessage(),
                    null, "", "");
        }
    }

    private void writeResultToMode(int status, boolean pushed,String msg, String sourceId,
                                   String apiParams, String apiResult) {
        Integer id = existWorkflowRecord();
        Map<String, Object> data = new HashMap<>(10);
        data.put(PUSHED_FIELD_NAME, pushed ? 1 : 0);
        data.put(PUSHED_DATE_FIELD_NAME, getDate());
        data.put(PUSH_CONFIG, configId);
        data.put(MSG_FIELD_NAME, msg);
        data.put(STATUS_FIELD_NAME, status);
        data.put(SOURCE_ID, sourceId);
        data.put(API_PARAMS, apiParams);
        data.put(API_RESULT, apiResult);
        data.put(IS_HISTORY_WORKFLOW, isHistoryWorkflow ? 1 : 0);
        if (id == null) {
            data.put(WORKFLOW_FIELD_NAME, requestId);
            if (!ModeUtil.insertToMode(data, TABLE_NAME, recordSet)) {
                log.error("插入推送结果到台账失败");
            }
        } else {
            List<Object> params = new ArrayList<>(data.values());
            params.add(id);
            if (!recordSet.executeUpdate("update " + TABLE_NAME + " set " +
                    SqlUtil.buildUpdateSql(new ArrayList<>(data.keySet())) + " where id=?", params)) {
                log.error("更新推送结果到台账失败");
            }
        }
    }

    private Integer existWorkflowRecord() {
        recordSet.executeQuery("select id from " + TABLE_NAME + " where " + WORKFLOW_FIELD_NAME + "=?", requestId);
        if (recordSet.next()) {
            return recordSet.getInt("id");
        }
        return null;
    }

    private String getDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return formatter.format(LocalDateTime.now());
    }

}
