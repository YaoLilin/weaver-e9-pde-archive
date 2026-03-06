package com.customization.yll.wuling.archive.data;

import com.customization.yll.wuling.archive.entity.ArchivePushRecordEntity;

import java.util.List;

/**
 * @author 姚礼林
 * @desc 档案推送记录, 读取建模表数据
 * @date 2025/9/30
 **/
public interface ArchivePushRecordService {

    /**
     * 获取档案系统未反馈的档案推送记录
     *
     * @return 档案系统未反馈的档案推送记录
     */
    List<ArchivePushRecordEntity> getNotFeedbackRecord();
}
