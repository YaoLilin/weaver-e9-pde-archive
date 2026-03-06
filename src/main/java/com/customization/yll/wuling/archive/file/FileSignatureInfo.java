package com.customization.yll.wuling.archive.file;

/**
 * @author 姚礼林
 * @desc 文件签名信息
 * @date 2025/8/12
 **/
public class FileSignatureInfo {
    /**
     * 签名时间
     */
    private String time;

    /**
     * 证书颁发机构
     */
    private String organization;

    /**
     * 证书序列号
     */
    private String certSerialNo;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 签名主体
     */
    private String signatory;

    /**
     * 签名原因
     */
    private String signReason;

    /**
     * 签名算法
     */
    private String strAlgName;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getCertSerialNo() {
        return certSerialNo;
    }

    public void setCertSerialNo(String certSerialNo) {
        this.certSerialNo = certSerialNo;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getSignatory() {
        return signatory;
    }

    public void setSignatory(String signatory) {
        this.signatory = signatory;
    }

    public String getSignReason() {
        return signReason;
    }

    public void setSignReason(String signReason) {
        this.signReason = signReason;
    }

    public String getStrAlgName() {
        return strAlgName;
    }

    public void setStrAlgName(String strAlgName) {
        this.strAlgName = strAlgName;
    }
}
