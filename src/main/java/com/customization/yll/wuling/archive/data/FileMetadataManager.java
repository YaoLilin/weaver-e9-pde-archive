package com.customization.yll.wuling.archive.data;

import com.customization.yll.common.workflow.WorkflowFieldValueManager;
import com.customization.yll.wuling.archive.config.ConfigurationModeDataManager;
import com.customization.yll.wuling.archive.entity.FileMetadataConfEntity;
import com.customization.yll.wuling.archive.util.CommonArchiveDataUtil;

/**
 * @author 姚礼林
 * @desc 获取文件实体元数据
 * @date 2025/10/31
 **/
public class FileMetadataManager extends AbstractFileMetadataManager {
    public FileMetadataManager(ConfigurationModeDataManager configurationModeDataManager,
                               WorkflowFieldValueManager fieldValueManager, Integer requestId) {
        super(configurationModeDataManager, fieldValueManager, requestId);
    }

    @Override
    String putCodeFixedValue(FileMetadataConfEntity entity) {
        if ("SYS_SOURCE_ID".equals(entity.getName())) {
            // SYS_SOURCE_ID 主键，数据来源ID，例：OA2024032300001
            return "OA" + getRequestId();
        }
        if ("sourceUniqueId".equals(entity.getName())) {
            // SYS_SOURCE_ID 主键，数据来源ID，例：OA2024032300001
            return "OA" + getRequestId();
        }
        if ("录入日期".equals(entity.getName())) {
            return CommonArchiveDataUtil.getCurrentDate();
        }
        if ("年度".equals(entity.getName())) {
            return CommonArchiveDataUtil.getYear();
        }
        return "";
    }
}
