package com.engine.interfaces.yll2.wuling.archive.service.impl;

import com.engine.interfaces.yll2.wuling.archive.bean.PushWorkflowInfo;
import com.engine.interfaces.yll2.wuling.archive.service.ArchiveHistoryWorkflowService;
import weaver.conn.RecordSet;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 姚礼林
 * @desc 档案集成历史流程业务
 * @date 2025/11/3
 **/
public class ArchiveHistoryWorkflowServiceImpl implements ArchiveHistoryWorkflowService {

    @Override
    public List<PushWorkflowInfo> getPushHistoryWorkflowList(List<Integer> workflowIds) {
        RecordSet recordSet = new RecordSet();
        List<PushWorkflowInfo> result = new ArrayList<>();
        String querySql = "select r.requestid,c.id from workflow_requestbase r " +
                " join uf_achive_config c on c.workflow=r.workflowid " +
                " where r.workflowid=? and r.currentnodetype='3'" +
                " and (EXISTS (select id from uf_record_result u where u.workflow=r.requestid and u.status not in " +
                " (1,2,3,4,6) and u.is_history=1 )" +
                " or not EXISTS(select id from uf_record_result u where u.workflow=r.requestid))";
        for (Integer workflowId : workflowIds) {
            recordSet.executeQuery(querySql, workflowId);
            while (recordSet.next()) {
                PushWorkflowInfo pushWorkflowInfo = new PushWorkflowInfo();
                pushWorkflowInfo.setRequestId(recordSet.getInt("requestid"));
                pushWorkflowInfo.setConfigId(recordSet.getInt("id"));
                result.add(pushWorkflowInfo);
            }
        }
        return result;
    }
}
