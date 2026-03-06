package com.customization.yll.wuling.archive.entity;

import com.customization.yll.wuling.archive.constants.BizState;
import lombok.Data;

/**
 * @author yaolilin
 * @desc 建模业务实体元数据明细数据
 * @date 2024/9/2
 **/
@Data
public class BusinessMetadataDetailEntity {
    private String  nodeIds;
    private String activity;
    private BizState bizState;
    private String actionMandate;
    private String description;
}
