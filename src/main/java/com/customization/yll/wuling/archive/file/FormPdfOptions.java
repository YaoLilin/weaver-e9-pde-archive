package com.customization.yll.wuling.archive.file;

import com.customization.yll.wuling.archive.constants.FormPdfNodePositionType;

/**
 * @author yaolilin
 * @desc 生成表单pdf选项
 * @date 2024/12/10
 **/
public class FormPdfOptions {
    private boolean isCreateFormPdf;
    private FormPdfNodePositionType formPdfNodePositionType;
    private Integer formPdfNodeId;

    public FormPdfOptions(boolean isCreateFormPdf, FormPdfNodePositionType formPdfNodePositionType, Integer formPdfNodeId) {
        this.isCreateFormPdf = isCreateFormPdf;
        this.formPdfNodePositionType = formPdfNodePositionType;
        this.formPdfNodeId = formPdfNodeId;
    }

    public boolean isCreateFormPdf() {
        return isCreateFormPdf;
    }

    public FormPdfNodePositionType getFormPdfNodePositionType() {
        return formPdfNodePositionType;
    }

    public Integer getFormPdfNodeId() {
        return formPdfNodeId;
    }
}
