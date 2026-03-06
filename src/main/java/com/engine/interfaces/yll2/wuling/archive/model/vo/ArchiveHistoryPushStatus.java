package com.engine.interfaces.yll2.wuling.archive.model.vo;

import lombok.Data;

/**
 * @author yaolilin
 * @desc 历史档案推送状态信息
 * @date 2024/11/26
 **/
@Data
public class ArchiveHistoryPushStatus {
    private boolean isPushing;
    private int totalCount;
    private int doneCount;
    private int failedCount;
}
