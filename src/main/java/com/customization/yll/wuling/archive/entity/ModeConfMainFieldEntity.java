package com.customization.yll.wuling.archive.entity;

import com.customization.yll.common.workflow.constants.WorkflowType;
import com.customization.yll.wuling.archive.constants.FormPdfNodePositionType;

import java.util.List;

/**
 * @author yaolilin
 * @desc 配置建模主表字段值
 * @date 2024/8/26
 **/
public class ModeConfMainFieldEntity {
    private Integer id;
    private Integer workflowId;
    private String workflowTableName;
    private WorkflowType workflowType;
    private Integer mainBody;
    private Integer imageFile;
    private List<Integer> otherImageFiles;
    private List<Integer> hlgDocs;
    private boolean isAttachmentInMainBody;
    private boolean isCreateFormPdf;
    /**
     * 表单pdf生成节点位置类型
     */
    private FormPdfNodePositionType formPdfNodePositionType;
    /**
     * 选择生成表单pdf的节点
     */
    private Integer formPdfNodeId;
    private boolean mainDocRequired;
    private boolean attachmentRequired;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(Integer workflowId) {
        this.workflowId = workflowId;
    }

    public String getWorkflowTableName() {
        return workflowTableName;
    }

    public void setWorkflowTableName(String workflowTableName) {
        this.workflowTableName = workflowTableName;
    }

    public WorkflowType getWorkflowType() {
        return workflowType;
    }

    public void setWorkflowType(WorkflowType workflowType) {
        this.workflowType = workflowType;
    }

    public Integer getMainBody() {
        return mainBody;
    }

    public void setMainBody(Integer mainBody) {
        this.mainBody = mainBody;
    }

    public Integer getImageFile() {
        return imageFile;
    }

    public void setImageFile(Integer imageFile) {
        this.imageFile = imageFile;
    }

    public List<Integer> getOtherImageFiles() {
        return otherImageFiles;
    }

    public void setOtherImageFiles(List<Integer> otherImageFiles) {
        this.otherImageFiles = otherImageFiles;
    }

    public boolean isAttachmentInMainBody() {
        return isAttachmentInMainBody;
    }

    public void setAttachmentInMainBody(boolean attachmentInMainBody) {
        isAttachmentInMainBody = attachmentInMainBody;
    }

    public boolean isCreateFormPdf() {
        return isCreateFormPdf;
    }

    public void setCreateFormPdf(boolean createFormPdf) {
        isCreateFormPdf = createFormPdf;
    }

    public FormPdfNodePositionType getFormPdfNodePositionType() {
        return formPdfNodePositionType;
    }

    public void setFormPdfNodePositionType(FormPdfNodePositionType formPdfNodePositionType) {
        this.formPdfNodePositionType = formPdfNodePositionType;
    }

    public Integer getFormPdfNodeId() {
        return formPdfNodeId;
    }

    public void setFormPdfNodeId(Integer formPdfNodeId) {
        this.formPdfNodeId = formPdfNodeId;
    }

    public boolean isMainDocRequired() {
        return mainDocRequired;
    }

    public void setMainDocRequired(boolean mainDocRequired) {
        this.mainDocRequired = mainDocRequired;
    }

    public boolean isAttachmentRequired() {
        return attachmentRequired;
    }

    public void setAttachmentRequired(boolean attachmentRequired) {
        this.attachmentRequired = attachmentRequired;
    }

    public List<Integer> getHlgDocs() {
        return hlgDocs;
    }

    public void setHlgDocs(List<Integer> hlgDocs) {
        this.hlgDocs = hlgDocs;
    }
}
