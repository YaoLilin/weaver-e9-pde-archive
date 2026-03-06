package com.customization.yll.wuling.archive.job;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.customization.yll.common.IntegrationLog;
import com.customization.yll.common.exception.CronJobException;
import com.customization.yll.common.web.exception.ApiCallException;
import com.customization.yll.common.web.exception.ApiResultFailedException;
import com.customization.yll.wuling.archive.api.QueryArchiveStatusParam;
import com.customization.yll.wuling.archive.api.QueryArchiveStatusService;
import com.customization.yll.wuling.archive.api.QueryArchiveStatusServiceImpl;
import com.customization.yll.wuling.archive.config.ArchiveConfig;
import com.customization.yll.wuling.archive.data.ArchivePushRecordService;
import com.customization.yll.wuling.archive.data.ArchivePushRecordServiceImpl;
import com.customization.yll.wuling.archive.entity.ArchivePushRecordEntity;
import com.customization.yll.wuling.archive.exception.ArchiveStatusRecordException;
import com.engine.interfaces.yll2.wuling.archive.util.ArchiveApiResultVerifyUtil;
import org.jetbrains.annotations.NotNull;
import weaver.conn.RecordSet;
import weaver.interfaces.schedule.BaseCronJob;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author 姚礼林
 * @desc 定期检查档案归档状态抽象类，调用档案系统接口获取归档状态并进行记录
 * @date 2025/10/9
 **/
public abstract class AbstractArchiveStatusCheckJob extends BaseCronJob {
    private final IntegrationLog log = new IntegrationLog(this.getClass());
    private final ArchivePushRecordService recordService;
    private final QueryArchiveStatusService queryArchiveStatusService;
    private final RecordSet recordSet;

    protected AbstractArchiveStatusCheckJob(ArchivePushRecordService recordService,
                                            QueryArchiveStatusService queryArchiveStatusService,
                                            RecordSet recordSet) {
        this.recordService = recordService;
        this.queryArchiveStatusService = queryArchiveStatusService;
        this.recordSet = recordSet;
    }

    protected AbstractArchiveStatusCheckJob() {
        this.recordSet = new RecordSet();
        this.recordService = new ArchivePushRecordServiceImpl(recordSet);
        this.queryArchiveStatusService = new QueryArchiveStatusServiceImpl();
    }

    @Override
    public void execute() {
        try {
            String apiUrl = ArchiveConfig.getQueryStatusUrl();
            List<ArchivePushRecordEntity> notFeedbackRecords = recordService.getNotFeedbackRecord();
            log.info("待反馈的归档结果记录数：" + notFeedbackRecords.size());
            // 归档状态查询接口一次最多只能查20条，所以分20条调用一次
            List<List<ArchivePushRecordEntity>> splitRecords = CollUtil.split(notFeedbackRecords, 20);
            int count = 1;
            for (List<ArchivePushRecordEntity> records : splitRecords) {
                log.info("查询批次：{} / {}", count, splitRecords.size());
                QueryArchiveStatusParam param = buildParam(records);
                log.info("档案状态查询参数：" + JSON.toJSONString(param));

                String apiResult = queryArchiveStatusService.queryStatus(ArchiveConfig.getServerAddress(), apiUrl,
                        param);
                if (StrUtil.isEmpty(apiResult)) {
                    throw new ApiCallException("调用档案状态查询接口出错");
                }
                log.info("档案状态查询接口返回结果：" + apiResult);
                ArchiveApiResultVerifyUtil.VerifyResult verify = ArchiveApiResultVerifyUtil.verify(apiResult);
                if (!verify.isSuccess()) {
                    throw new ApiResultFailedException("接口调用失败，返回信息：" + verify.getMessage());
                }

                try {
                    recordStatus(apiResult);
                    log.info("已记录归档状态到建模");
                } catch (Exception e) {
                    throw new ArchiveStatusRecordException("归档结果记录出错：" + e.getMessage(), e);
                }
                count++;
            }
        } catch (Exception e) {
            log.error("计划任务执行出错：" + e.getMessage(), e);
            throw new CronJobException("计划任务执行出错：" + e.getMessage(), e);
        }

    }

    /**
     * 记录档案的归档状态
     *
     * @param statusApiResult 档案系统接口返回的归档结果
     */
    protected abstract void recordStatus(String statusApiResult);

    @NotNull
    private QueryArchiveStatusParam buildParam(List<ArchivePushRecordEntity> notFeedbackRecords) {
        List<String> sourceUniqueIds = new ArrayList<>();
        for (ArchivePushRecordEntity pushRecord : notFeedbackRecords) {
            sourceUniqueIds.add(pushRecord.getSourceId());
        }

        return new QueryArchiveStatusParam(
                DateUtil.format(new Date(), "yyyyMMddHHmmssSSS"),
                ArchiveConfig.getCode(), sourceUniqueIds);
    }
}
