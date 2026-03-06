package com.engine.interfaces.yll2.wuling.archive.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author yaolilin
 * @desc 历史档案推送结果
 * @date 2025/1/13
 **/
@Data
@AllArgsConstructor
public class HistoryArchivePushResult {
    private String message;
    private boolean success;
    private boolean hadPushData;
}
