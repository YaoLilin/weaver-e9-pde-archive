package com.customization.yll.wuling.archive.data;

import com.customization.yll.common.exception.ConfigurationException;
import com.customization.yll.common.util.HrmInfoUtil;
import com.customization.yll.common.workflow.WorkflowApprovalInfoManager;
import com.customization.yll.common.workflow.entity.WorkflowApprovalInfoEntity;
import com.customization.yll.wuling.archive.config.ConfigurationModeDataManager;
import com.customization.yll.wuling.archive.entity.BusinessMetadataDetailEntity;
import com.customization.yll.wuling.archive.exception.ArchiveDataException;
import com.pde.pdes.eep.domian.metadata.BusinessEntity;
import org.apache.commons.lang.StringUtils;
import weaver.conn.RecordSet;
import weaver.general.Util;
import weaver.integration.logging.Logger;
import weaver.integration.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 获取业务实体元数据，取流程流转意见
 * @author yaolilin
 */
public class BusinessMetadataManager {
    private List<BusinessEntity> businessEntities;
    private final WorkflowApprovalInfoManager approvalInfoManager =  new WorkflowApprovalInfoManager();
    private final ConfigurationModeDataManager modeDataManager;
    private final int requestId;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public BusinessMetadataManager(ConfigurationModeDataManager modeDataManager, int requestId) {
        this.modeDataManager = modeDataManager;
        this.requestId = requestId;
    }

    /**
     * 获取业务实体元数据
     * @return 业务实体元数据
     */
    protected List<BusinessEntity> getBusinessEntities() {
        if (businessEntities == null) {
            verifyWorkflowId();
            businessEntities = buildBusinessEntities();
        }
        return businessEntities;
    }

    /**
     * 从流程的审批信息（签字意见）中获取业务实体元数据
     * @return 业务实体元数据
     */
    private List<BusinessEntity> buildBusinessEntities() {
        businessEntities = new ArrayList<>();
        RecordSet hrmRs = new RecordSet();
        Map<Integer, BusinessMetadataDetailEntity> businessMetadataMap = getBusinessMetadataMap();
        List<WorkflowApprovalInfoEntity> approvalInfoList = approvalInfoManager.getWorkflowApprovalInfo(requestId);
        int order = 1;
        // 遍历所有审批意见，找到审批意见节点中对应的业务实体元数据配置
        for (WorkflowApprovalInfoEntity info : approvalInfoList) {
            String dateTime = info.getOperateDate() + " " + info.getOperateTime();
            BusinessMetadataDetailEntity metadataDetailEntity = businessMetadataMap.get(info.getNodeId());
            if (metadataDetailEntity == null) {
                log.warn(String.format("该节点没有配置业务行为，请前往配置建模配置节点的业务行为，nodeId:%s", info.getNodeId()));
                continue;
            }
            String workCodeOrId = HrmInfoUtil.getWorkCode(info.getOperator(), hrmRs);
            if (StringUtils.isEmpty(workCodeOrId)) {
                // 没有工号就用人员id
                workCodeOrId = info.getOperator()+"";
            }
            businessEntities.add(getBusinessEntities(order++, workCodeOrId, metadataDetailEntity, dateTime));
        }
        if (businessEntities.isEmpty()) {
            throw new ArchiveDataException("获取业务实体元数据失败，找不到审批意见");
        }
        return businessEntities;
    }

    private void  verifyWorkflowId() {
        RecordSet recordSet = new RecordSet();
        recordSet.executeQuery("select workflowid from workflow_requestbase where requestid=?", requestId);
        recordSet.next();
        Integer workflowId = modeDataManager.getMainFieldEntity().getWorkflowId();
        if (recordSet.getInt("workflowid") != workflowId) {
            throw new ConfigurationException( "当前流程与档案推送配置的流程不符合，请检查是否是同一类型流程以及流程版本是否一致");
        }
    }

    /**
     * 获取建模中配置的节点对应的业务实体元数据配置
     * @return 业务实体元数据配置，key为节点id，value为业务实体元数据配置
     */
    private Map<Integer, BusinessMetadataDetailEntity> getBusinessMetadataMap() {
        // 建模中业务实体元数据配置的节点id为多选，所以要分离到单个节点id，转成每个节点id对应的元数据配置
        List<BusinessMetadataDetailEntity> businessMetadataDetail = modeDataManager.getBusinessMetadataDetail();
        Map<Integer, BusinessMetadataDetailEntity> map = new HashMap<>(10);
        for (BusinessMetadataDetailEntity detail : businessMetadataDetail) {
            String[] nodeIds = detail.getNodeIds().split(",");
            for (String nodeId : nodeIds) {
                if (nodeId.isEmpty()) {
                    continue;
                }
                if (!map.containsKey(Integer.parseInt(nodeId))) {
                    BusinessMetadataDetailEntity entity = new BusinessMetadataDetailEntity();
                    entity.setNodeIds(nodeId);
                    entity.setDescription(detail.getDescription());
                    entity.setActivity(detail.getActivity());
                    entity.setActionMandate(detail.getActionMandate());
                    entity.setBizState(detail.getBizState());
                    map.put(Integer.parseInt(nodeId), entity);
                }
            }
        }
        return map;
    }


    private BusinessEntity getBusinessEntities(int order, String workCodeOrId,
                                               BusinessMetadataDetailEntity metadataDetailEntity, String dateTime) {
        BusinessEntity entity = new BusinessEntity();
        //业务标识符:业务标识符M227，业务系统中流程审批顺序，为自然数
        entity.setBizId(order + "");
        //机构人员标识符:机构人员标识符M228，业务处理人员机构员工系统ID
        entity.setAgentId(workCodeOrId);
        //文件标识符:文件标识符M223，业务系统推送电子文件的序号，填写自然数，自增长。默认空
        entity.setDocId("");
        //业务状态：电子文件形成、处理和管理等业务行为的时态类型，值域：历史行为；计划任务。与业务系统对接进行文件归档时，默认历史行为。
        entity.setBizStatus(metadataDetailEntity.getBizState().getName());
        //业务行为：行电子文件形成、处理、管理等业务的具体行为，文书类的值域：草拟,审核,签发,会签,复核,缮印,用印,登记,分发,签收,拟办,批办,承办,催办,价值鉴定,整理,立卷,归档等等，其他
        entity.setBizActivity(metadataDetailEntity.getActivity());
        //行为时间：实施具体业务行为的时间或时间段，yyyy-MM-dd HH:mm:ss
        entity.setActionTime(dateTime);
        //行为依据：实施具体业务行为的依据、授权或原因
        entity.setActionMandate(Util.null2String(metadataDetailEntity.getActionMandate()));
        //行为描述：业务行为的相关描述
        entity.setActionDescription(Util.null2String(metadataDetailEntity.getDescription()));
        return entity;
    }


}
