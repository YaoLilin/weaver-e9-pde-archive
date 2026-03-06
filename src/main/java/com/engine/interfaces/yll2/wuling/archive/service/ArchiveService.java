package com.engine.interfaces.yll2.wuling.archive.service;

import com.alibaba.fastjson.JSONObject;
import com.engine.interfaces.yll2.wuling.archive.model.dto.HistoryArchivePushResult;
import com.engine.interfaces.yll2.wuling.archive.model.vo.ArchiveHistoryPushStatus;

import java.util.List;

/**
 * @author 姚礼林
 * @desc 档案集成业务接口
 * @date 2025/11/3
 **/
public interface ArchiveService {

    /**
     * 接收档案系统推送过来的档案归档结果数据，将档案归档状态记录到建模中
     *
     * @param params 档案系统推送过来的档案归档结果数据
     */
    void recordStatus(JSONObject params);

    /**
     * 推送流程的档案到档案系统
     *
     * @param configId          配置建模的数据id
     * @param requestId         流程请求id
     * @param isHistoryWorkflow 是否历史流程
     */
    void push(int configId, int requestId, boolean isHistoryWorkflow);

    /**
     * 推送历史流程档案到档案系统
     *
     * @param workflowIds 需要推送的流程id集合
     * @return 历史流程档案推送结果
     */
    HistoryArchivePushResult pushHistoryWorkflow(List<Integer> workflowIds);

    /**
     * 停止推送历史流程档案，如果历史流程档案正在推送中，则停止推送
     */
    void stopHistoryPush();

    /**
     * 获取历史流程档案推送状态，在前端历史流程档案推送显示页面中，可由此方法获取推送状态
     *
     * @return 推送状态
     */
    ArchiveHistoryPushStatus getHistoryPushStatus();
}
