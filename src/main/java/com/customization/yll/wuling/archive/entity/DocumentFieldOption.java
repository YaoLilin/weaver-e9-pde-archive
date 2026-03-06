package com.customization.yll.wuling.archive.entity;

import java.util.List;

/**
 * 流程中正文和附件字段的值
 * @author yaolilin
 */
public class DocumentFieldOption {
    /**
     * 附件字段值，如果有多个字段，则值合并在一起
     */
    private List<Integer> imageFileValue;
    private List<Integer> mainBodyFieldValue;
    /**
     * 花脸稿字段字段值，如果有多个字段，则值合并在一起
     */
    private List<Integer> hlgFieldValue;
    private boolean mainDocRequired;
    private boolean attachmentRequired;

    public List<Integer> getImageFileValue() {
        return imageFileValue;
    }

    public void setImageFileValue(List<Integer> imageFileValue) {
        this.imageFileValue = imageFileValue;
    }

    public List<Integer> getMainBodyFieldValue() {
        return mainBodyFieldValue;
    }

    public void setMainBodyFieldValue(List<Integer> mainBodyFieldValue) {
        this.mainBodyFieldValue = mainBodyFieldValue;
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

    public List<Integer> getHlgFieldValue() {
        return hlgFieldValue;
    }

    public void setHlgFieldValue(List<Integer> hlgFieldValue) {
        this.hlgFieldValue = hlgFieldValue;
    }
}
