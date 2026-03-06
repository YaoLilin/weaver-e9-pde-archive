package com.customization.yll.wuling.archive.config;

import java.util.Optional;

/**
 * @author 姚礼林
 * @desc 档案推送建模配置业务类接口
 * @date 2025/10/9
 **/
public interface ArchiveConfigModeService {

    /**
     * 根据流程id获取对应的档案推送配置id
     *
     * @param workflowId 流程id
     * @return 档案推送配置id
     */
    Optional<Integer> getConfigId(int workflowId);
}
