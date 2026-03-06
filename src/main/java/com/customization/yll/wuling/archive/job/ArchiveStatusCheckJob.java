package com.customization.yll.wuling.archive.job;

import com.alibaba.fastjson.JSON;
import com.customization.yll.wuling.archive.api.QueryArchiveStatusService;
import com.customization.yll.wuling.archive.data.ArchivePushRecordService;
import com.engine.interfaces.yll2.wuling.archive.service.ArchiveService;
import com.engine.interfaces.yll2.wuling.archive.service.ArchiveStatusApiResultAnalysisHelper;
import com.engine.interfaces.yll2.wuling.archive.service.impl.ArchiveServiceImpl;
import lombok.Setter;
import weaver.conn.RecordSet;

/**
 * @author 姚礼林
 * @desc 定期检查档案归档状态，调用档案系统接口获取归档状态并写入到归档结果台账内
 * @date 2025/9/30
 **/
@Setter
public class ArchiveStatusCheckJob extends AbstractArchiveStatusCheckJob {
    private final ArchiveStatusApiResultAnalysisHelper apiResultAnalysisHelper;
    private final ArchiveService archiveService;

    protected ArchiveStatusCheckJob(ArchivePushRecordService recordService,
                                    QueryArchiveStatusService queryArchiveStatusService,
                                    RecordSet recordSet, ArchiveStatusApiResultAnalysisHelper apiResultAnalysisHelper,
                                    ArchiveService archiveService) {
        super(recordService, queryArchiveStatusService, recordSet);
        this.apiResultAnalysisHelper = apiResultAnalysisHelper;
        this.archiveService = archiveService;
    }

    public ArchiveStatusCheckJob() {
        apiResultAnalysisHelper = new ArchiveStatusApiResultAnalysisHelper();
        archiveService = new ArchiveServiceImpl(apiResultAnalysisHelper);
    }

    @Override
    protected void recordStatus(String statusApiResult) {
        archiveService.recordStatus(JSON.parseObject(statusApiResult));
    }
}
