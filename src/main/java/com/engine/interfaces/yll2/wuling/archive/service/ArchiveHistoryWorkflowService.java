package com.engine.interfaces.yll2.wuling.archive.service;

import com.engine.interfaces.yll2.wuling.archive.bean.PushWorkflowInfo;

import java.util.List;

/**
 * @author 姚礼林
 * @desc 档案集成历史流程业务接口
 * @date 2025/11/3
 **/
public interface ArchiveHistoryWorkflowService {

    /**
     * 获取需要推送档案的历史流程信息
     *
     * @param workflowIds 需要推送的流程id集合
     * @return 需要推送档案的历史流程信息
     */
    List<PushWorkflowInfo> getPushHistoryWorkflowList(List<Integer> workflowIds);
}
