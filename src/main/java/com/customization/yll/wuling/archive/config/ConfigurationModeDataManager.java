package com.customization.yll.wuling.archive.config;

import cn.hutool.core.convert.Convert;
import com.customization.yll.common.exception.ConfigModeDataNotFoundException;
import com.customization.yll.common.exception.ConfigurationException;
import com.customization.yll.common.exception.FieldValueEmptyException;
import com.customization.yll.common.util.WorkflowUtil;
import com.customization.yll.common.workflow.constants.GetWorkflowFieldDataWay;
import com.customization.yll.common.workflow.constants.SystemParam;
import com.customization.yll.common.workflow.constants.WorkflowType;
import com.customization.yll.wuling.archive.constants.BizState;
import com.customization.yll.wuling.archive.constants.ConfigModeInfo;
import com.customization.yll.wuling.archive.constants.FormPdfNodePositionType;
import com.customization.yll.wuling.archive.entity.BusinessMetadataDetailEntity;
import com.customization.yll.wuling.archive.entity.FileMetadataConfEntity;
import com.customization.yll.wuling.archive.entity.ModeConfMainFieldEntity;
import com.customization.yll.wuling.archive.exception.WorkflowTableNotFoundException;
import org.apache.commons.lang.StringUtils;
import weaver.conn.RecordSet;

import java.util.ArrayList;
import java.util.List;

import static com.customization.yll.wuling.archive.constants.ConfigModeInfo.CONFIG_MODE_TABLE;

/**
 * 获取配置建模中的数据
 *
 * @author yaolilin
 */
public class ConfigurationModeDataManager {
    private final int configId;
    private ModeConfMainFieldEntity mainFieldEntity;
    private List<BusinessMetadataDetailEntity> businessMetadataDetail;
    private List<FileMetadataConfEntity> fileMetadataConfDetail;

    public ConfigurationModeDataManager(int configId) {
        this.configId = configId;
    }

    public ModeConfMainFieldEntity getMainFieldEntity() {
        if (mainFieldEntity != null) {
            return mainFieldEntity;
        }
        RecordSet recordSet = new RecordSet();
        recordSet.executeQuery("select * from " + CONFIG_MODE_TABLE + " where id=?", configId);
        if (!recordSet.next()) {
            throw new ConfigModeDataNotFoundException("未找到建模配置信息，请检查建模数据id是否正确，数据id：" + configId);
        }
        String otherImageFiles = recordSet.getString("other_image_files");
        List<Integer> otherImageFileList = null;
        if (!otherImageFiles.isEmpty()) {
            otherImageFileList = Convert.toList(Integer.class, otherImageFiles.split(","));
        }
        String hlgDocFieldValue = recordSet.getString("hlg_doc");
        List<Integer> hlgDocs = null;
        if (!hlgDocFieldValue.isEmpty()) {
            hlgDocs = Convert.toList(Integer.class, hlgDocFieldValue.split(","));
        }
        mainFieldEntity = new ModeConfMainFieldEntity();
        mainFieldEntity.setId(configId);
        mainFieldEntity.setMainBody(Convert.toInt(recordSet.getString("main_body")));
        mainFieldEntity.setImageFile(Convert.toInt(recordSet.getString("image_file")));
        mainFieldEntity.setOtherImageFiles(otherImageFileList);
        mainFieldEntity.setHlgDocs(hlgDocs);
        mainFieldEntity.setWorkflowId(Convert.toInt(recordSet.getString("workflow")));
        Integer workflowId = mainFieldEntity.getWorkflowId();
        if (workflowId != null) {
            mainFieldEntity.setWorkflowTableName(WorkflowUtil.getWorkflowTableName(workflowId, new RecordSet()));
        }
        String workflowType = recordSet.getString("workflow_type");
        if (!StringUtils.isEmpty(workflowType)) {
            mainFieldEntity.setWorkflowType(WorkflowType.of(Integer.parseInt(workflowType)));
        }
        mainFieldEntity.setAttachmentInMainBody(recordSet.getInt("is_main_body_attac") == 1);
        mainFieldEntity.setCreateFormPdf(recordSet.getInt("create_form_pdf") == 1);
        String formPdfNodePositionType = recordSet.getString("form_pdf_node_type");
        if (StringUtils.isEmpty(formPdfNodePositionType)) {
            mainFieldEntity.setFormPdfNodePositionType(FormPdfNodePositionType.CURRENT_NODE);
        } else if ("0".equals(formPdfNodePositionType)) {
            mainFieldEntity.setFormPdfNodePositionType(FormPdfNodePositionType.CURRENT_NODE);
        }else {
            mainFieldEntity.setFormPdfNodePositionType(FormPdfNodePositionType.SELECT_NODE);
        }
        mainFieldEntity.setFormPdfNodeId(Convert.toInt(recordSet.getString("form_pdf_node_select")));
        mainFieldEntity.setMainDocRequired(recordSet.getInt("main_doc_required") == 1);
        mainFieldEntity.setAttachmentRequired(recordSet.getInt("attachment_required") == 1);
        verifyField(workflowId + "");
        return mainFieldEntity;
    }

    /**
     * 从配置建模中明细表读取节点对应的业务行为
     *
     * @return map, key为节点id，value为节点对应的业务行为
     */
    public List<BusinessMetadataDetailEntity> getBusinessMetadataDetail() {
        if (businessMetadataDetail != null) {
            return businessMetadataDetail;
        }
        RecordSet recordSet = new RecordSet();
        businessMetadataDetail = new ArrayList<>();
        recordSet.executeQuery("select node,ativity,biz_status,action_mandate,description from "
                + ConfigModeInfo.CONFIG_MODE_TABLE + "_dt3 where mainid =?", configId);
        if (!recordSet.next()) {
            throw new ConfigurationException("配置建模中没有配置业务实体元数据");
        }
        do {
            BusinessMetadataDetailEntity entity = new BusinessMetadataDetailEntity();
            entity.setNodeIds(recordSet.getString("node"));
            entity.setActivity(recordSet.getString("ativity"));
            entity.setActionMandate(recordSet.getString("action_mandate"));
            String bizStatus = recordSet.getString("biz_status");
            if (!StringUtils.isEmpty(bizStatus)) {
                if ("0".equals(bizStatus)) {
                    entity.setBizState(BizState.HISTORY);
                } else if ("1".equals(bizStatus)) {
                    entity.setBizState(BizState.PLAN_TASK);
                }
            } else {
                entity.setBizState(BizState.HISTORY);
            }
            entity.setDescription(recordSet.getString("description"));
            verifyBusinessEntityField(entity);
            businessMetadataDetail.add(entity);
        } while (recordSet.next());
        return businessMetadataDetail;
    }

    public List<FileMetadataConfEntity> getFileMetadataConfDetail() {
        if (fileMetadataConfDetail != null) {
            return fileMetadataConfDetail;
        }
        fileMetadataConfDetail = new ArrayList<>();
        RecordSet recordSet = new RecordSet();
        recordSet.executeQuery("select name,default_value,fix_value,sys_field,workflow_field,not_emtpy,is_fix," +
                "value_method from " + ConfigModeInfo.CONFIG_MODE_TABLE + "_dt4 where mainid =?", configId);
        while (recordSet.next()) {
            FileMetadataConfEntity entity = new FileMetadataConfEntity();
            entity.setName(recordSet.getString("name"));
            entity.setDefaultValue(recordSet.getString("default_value"));
            entity.setFixValue(recordSet.getString("fix_value"));
            String workflowField = recordSet.getString("workflow_field");
            entity.setWorkflowFieldId(StringUtils.isEmpty(workflowField) ? null : Integer.parseInt(workflowField));
            String valueMethod = recordSet.getString("value_method");
            if (!StringUtils.isEmpty(valueMethod)) {
                GetWorkflowFieldDataWay way = GetWorkflowFieldDataWay.of(Integer.valueOf(valueMethod));
                entity.setGetWorkflowFieldDataWay(way);
            }
            String sysField = recordSet.getString("sys_field");
            if (!StringUtils.isEmpty(sysField)) {
                entity.setSystemParam(SystemParam.of(Integer.valueOf(sysField)));
            }
            entity.setRequired(recordSet.getInt("not_emtpy") == 1);
            entity.setFixed(recordSet.getInt("is_fix") == 1);
            fileMetadataConfDetail.add(entity);
        }
        return fileMetadataConfDetail;
    }

    private void verifyBusinessEntityField(BusinessMetadataDetailEntity entity) {
        if (StringUtils.isEmpty(entity.getNodeIds())) {
            throw new FieldValueEmptyException("业务实体元数据明细中[选择节点]字段必填");
        }
        if (StringUtils.isEmpty(entity.getActivity())) {
            throw new FieldValueEmptyException("业务实体元数据明细中[业务行为]字段必填");
        }
    }

    private void verifyField(String workflowId) {
        if (mainFieldEntity.getWorkflowId() == null) {
            throw new FieldValueEmptyException("建模中没有配置流程");
        }
        if (mainFieldEntity.getWorkflowTableName() == null) {
            throw new WorkflowTableNotFoundException("找不到流程表名，流程ID：" + workflowId);
        }
    }

}
