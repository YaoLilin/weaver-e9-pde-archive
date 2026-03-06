package com.customization.yll.wuling.archive.entity;

import lombok.Data;

/**
 * @author 姚礼林
 * @desc 档案推送记录信息
 * @date 2025/9/30
 **/
@Data
public class ArchivePushRecordEntity {
    private String sourceId;
    private boolean pushed;
    private Integer status;
    private String msg;
    private String feedbackDate;
    private Integer requestId;
}
