package com.customization.yll.wuling.archive.file;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.customization.yll.common.IntegrationLog;
import com.customization.yll.common.doc.QysFileSignatureInfoManager;
import com.customization.yll.common.doc.bean.SignatureResult;
import com.customization.yll.common.web.exception.ApiCallException;
import com.customization.yll.common.web.util.ApiCallManager;
import com.customization.yll.common.web.util.QysSignatureUtil;
import com.customization.yll.wuling.archive.config.ArchiveConfig;
import com.customization.yll.wuling.archive.exception.HandleWorkflowFileException;
import weaver.conn.RecordSet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author 姚礼林
 * @desc 处理文件签名
 * @date 2025/8/13
 **/
public class FileSignatureHandler {
    private final IntegrationLog log = new IntegrationLog(FileSignatureHandler.class);
    private String serverAddress;
    private String token;
    private String secret;

    /**
     * 添加文件签名
     * @param fileInfos 流程取到的文件信息
     * @param requestId 流程请求id
     * @param fileSaveDirPath 下载的契约锁盖章文件保存目录路径
     * @throws HandleWorkflowFileException 添加签名发生的异常
     */
    public void addSignature(List<FileInfo> fileInfos, int requestId, String fileSaveDirPath)
            throws HandleWorkflowFileException{
        init();
        RecordSet recordSet = new RecordSet();
        QysSignatureDocDownloadManger signatureDocDownloadManger = new QysSignatureDocDownloadManger(serverAddress,
                token, secret);
        QysFileSignatureInfoManager signatureInfoManager = new QysFileSignatureInfoManager(serverAddress,
                token, secret);
        for (FileInfo file : fileInfos) {
            Optional<QysDocInfo> qysDocInfo = getSignatureDocId(file.getImageFileId(), requestId, recordSet);
            if (qysDocInfo.isPresent()) {
                log.info("此文件是签署文件，文档id：" + file.getDocId() + ",文件名称：" + file.getTitle());
                file.setSignatureFile(true);
                // 下载契约锁签署文档
                File signatureFile = signatureDocDownloadManger.downloadDoc(qysDocInfo.get().documentId, fileSaveDirPath);
                if (signatureFile != null) {
                    log.info("获取契约锁盖章文件成功，文件路径：" + signatureFile.getAbsolutePath());
                    file.setFilePath(signatureFile.getAbsolutePath());
                    List<FileSignatureInfo> signatureInfoList = getFileSignInfo(file, qysDocInfo.get().contractId,
                            signatureInfoManager);
                    if (signatureInfoList.isEmpty()) {
                        throw new HandleWorkflowFileException("无法获取文件签名，文件名称：" + file.getTitle());
                    }
                    file.setSignatureInfo(signatureInfoList);
                } else {
                    throw new HandleWorkflowFileException("下载契约锁盖章文件失败，文件名称：" + file.getTitle()
                            + ", 文档id：" + file.getDocId());
                }
            }
        }
    }

    private void init() {
        if (serverAddress == null) {
            this.serverAddress = ArchiveConfig.getQysServerAddress();
            this.token = ArchiveConfig.getQysToken();
            this.secret = ArchiveConfig.getQysSecret();
        }
    }

    /**
     * 获取契约锁签署文档id
     */
    private Optional<QysDocInfo> getSignatureDocId(int imageFileId, int requestId, RecordSet recordSet) {
        recordSet.executeQuery("select documentId,contractId from wf_qiyuesuoCreateDocLog where requestid =? and " +
                "imagefileid=?", requestId, imageFileId);
        if (!recordSet.next()) {
            return Optional.empty();
        }
        String documentId = recordSet.getString("documentId");
        if (documentId.isEmpty()) {
            return Optional.empty();
        }
        QysDocInfo qysFileInfo = new QysDocInfo();
        qysFileInfo.documentId = Long.parseLong(documentId);
        qysFileInfo.contractId = Long.parseLong(recordSet.getString("contractId"));
        return Optional.of(qysFileInfo);
    }

    /**
     * 获取文件签名信息
     * @param fileInfo 文件信息
     * @param contractId 合同ID
     * @param signatureInfoManager 签名信息管理器
     * @return 文件签名信息列表
     */
    List<FileSignatureInfo> getFileSignInfo(FileInfo fileInfo, long contractId,
                                            QysFileSignatureInfoManager signatureInfoManager) {
        log.info("获取文件签名信息，文件名称：" + fileInfo.getTitle());
        String signOperator = getSignOperator(contractId);
        if (signOperator.isEmpty()) {
            throw new HandleWorkflowFileException("添加文件签名失败，无法获取签名操作者");
        }

        List<FileSignatureInfo> signatureInfoList = new ArrayList<>();
        Optional<SignatureResult> resultOp = signatureInfoManager.getFileSignatureInfo(fileInfo.getFilePath());
        if (resultOp.isPresent()) {
            SignatureResult signatureResult = resultOp.get();
            if (CollUtil.isNotEmpty(signatureResult.getSignatureInfos())) {
                signatureResult.getSignatureInfos().forEach(signInfo -> {
                    FileSignatureInfo signatureInfo = new FileSignatureInfo();
                    signatureInfo.setOrganization(signInfo.getOrganization());
                    signatureInfo.setCertSerialNo(signInfo.getCertSerialNo());
                    signatureInfo.setSignatory(signInfo.getSignatory());
                    signatureInfo.setSignReason(signInfo.getSignReason());
                    signatureInfo.setTime(signInfo.getSignTime());
                    signatureInfo.setStrAlgName(signInfo.getStrAlgName());
                    signatureInfo.setOperator(signOperator);

                    signatureInfoList.add(signatureInfo);
                });
            } else {
                log.error("SignatureInfos 签名信息列表为空");
            }
        } else {
            log.info("文件无签名信息: " + fileInfo.getFilePath());
        }
        return signatureInfoList;
    }

    /**
     * 获取签名操作者
     * @param contractId 合同ID
     * @return 签名操作者名称
     */
    String getSignOperator(long contractId) {
        log.info("获取签名操作者，合同ID：" + contractId);
        ApiCallManager apiCallManager = new ApiCallManager();
        String result = apiCallManager.getResult(this.serverAddress + "/contract/detail?contractId=" +
                        contractId,null, QysSignatureUtil.getSignatureHead(this.token, this.secret));
        log.info("查询电子签约详情信息结果：" + result);
        if (StrUtil.isEmpty(result)) {
            throw new ApiCallException("查询电子签约详情信息失败，接口无返回结果");
        }
        JSONObject json = JSON.parseObject(result);
        if (json.getIntValue("code") != 0) {
            throw new ApiCallException("查询电子签约详情信息失败，信息：" + json.getString("message"));
        }
        try {
            JSONArray signatories = json.getJSONObject("contract").getJSONArray("signatories");
            if (CollUtil.isNotEmpty(signatories)) {
                JSONObject signatory = signatories.getJSONObject(0);
                JSONArray actions = signatory.getJSONArray("actions");
                if (CollUtil.isNotEmpty(actions)) {
                    Optional<Object> actionOp = actions.stream().filter(i -> {
                        JSONObject item = (JSONObject) i;
                        return "SIGNED".equals(item.getString("status"));
                    }).findAny();
                    if (actionOp.isPresent()) {
                        JSONObject action = (JSONObject) actionOp.get();
                        JSONArray operators = action.getJSONArray("actionOperators");
                        if (CollUtil.isNotEmpty(operators)) {
                            JSONObject operator = operators.getJSONObject(0);
                            return operator.getString("operatorName");
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取接口返回结果中的操作人异常，接口返回结果：" + result);
        }
        return "";
    }

    private static class QysDocInfo {
        private Long documentId;
        private Long contractId;
    }

}
