package com.customization.yll.wuling.archive.config;

import com.customization.yll.wuling.archive.constants.ConfigModeInfo;
import weaver.conn.RecordSet;

import java.util.Optional;

/**
 * @author 姚礼林
 * @desc 档案推送建模配置业务类
 * @date 2025/10/9
 **/
public class ArchiveConfigModeServiceImpl implements ArchiveConfigModeService {
    private final RecordSet recordSet;

    public ArchiveConfigModeServiceImpl(RecordSet recordSet) {
        this.recordSet = recordSet;
    }

    @Override
    public Optional<Integer> getConfigId(int workflowId) {
        recordSet.executeQuery("select id from " + ConfigModeInfo.CONFIG_MODE_TABLE + " where workflow=?", workflowId);
        if (!recordSet.next()) {
            return Optional.empty();
        }
        return Optional.of(recordSet.getInt("id"));
    }
}
