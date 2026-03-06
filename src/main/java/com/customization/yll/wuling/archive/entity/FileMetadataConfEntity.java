package com.customization.yll.wuling.archive.entity;

import com.customization.yll.common.workflow.constants.GetWorkflowFieldDataWay;
import com.customization.yll.common.workflow.constants.SystemParam;
import lombok.Data;

/**
 * @desc 配置建模中配置文件实体元数据的明细数据
 * @author yaolilin
 */
@Data
public class FileMetadataConfEntity {
    private String name;
    private String defaultValue;
    private String fixValue;
    private SystemParam systemParam;
    private Integer workflowFieldId;
    private GetWorkflowFieldDataWay getWorkflowFieldDataWay;
    private boolean required;
    private boolean isFixed;
}
