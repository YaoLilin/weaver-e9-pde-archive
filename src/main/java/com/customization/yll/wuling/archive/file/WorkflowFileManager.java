package com.customization.yll.wuling.archive.file;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.customization.yll.common.doc.DocConvertorByWpsApi;
import com.customization.yll.common.doc.DocFileManager;
import com.customization.yll.common.doc.bean.DocFileInfo;
import com.customization.yll.common.doc.constants.DocFileType;
import com.customization.yll.common.doc.util.AsposePDFConverter;
import com.customization.yll.common.doc.util.FileConvertUtil;
import com.customization.yll.common.exception.ConfigurationException;
import com.customization.yll.common.exception.DocConvertException;
import com.customization.yll.common.exception.PropNotConfigureException;
import com.customization.yll.common.util.DocUtil;
import com.customization.yll.common.workflow.WorkflowFormPdfCreator;
import com.customization.yll.wuling.archive.config.ArchiveConfig;
import com.customization.yll.wuling.archive.constants.FileConvertMethod;
import com.customization.yll.wuling.archive.constants.FileType;
import com.customization.yll.wuling.archive.constants.FormPdfNodePositionType;
import com.customization.yll.wuling.archive.entity.DocumentFieldOption;
import com.customization.yll.wuling.archive.exception.HandleWorkflowFileException;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import weaver.conn.RecordSet;
import weaver.integration.logging.Logger;
import weaver.integration.logging.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 获取流程中正文、附件字段存放的文件以及流程表单pdf文件，如果是doc文档则转换成pdf文件
 *
 * @author yaolilin
 * @date 2024-09-05
 */
public class WorkflowFileManager {
    private final DocumentFieldOption documentFieldOptions;
    private final String savePath;
    private final int requestId;
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final DocFileManager docFileManager = new DocFileManager();
    private final FileSignatureHandler fileSignatureHandler = new FileSignatureHandler();
    private List<FileInfo> workflowFiles;
    private final boolean isAttachmentInMainBody;
    private final FormPdfOptions formPdfOptions;

    /**
     * @param requestId              流程请求id
     * @param isAttachmentInMainBody 附件是否在正文文档的附件中，如果为是则附件从正文文档的附件获取
     * @param documentFieldOptions   流程中的正文和附件字段值
     * @param formPdfOptions         生成表单pdf选项
     * @param savePath               存放获取文件的存储路径
     */
    public WorkflowFileManager(int requestId, boolean isAttachmentInMainBody,
                               DocumentFieldOption documentFieldOptions, FormPdfOptions formPdfOptions, String savePath) {
        this.requestId = requestId;
        this.documentFieldOptions = documentFieldOptions;
        this.savePath = savePath;
        this.isAttachmentInMainBody = isAttachmentInMainBody;
        this.formPdfOptions = formPdfOptions;
    }

    /**
     * 获取流程的正文pdf、附件pdf以及流程表单pdf文件
     *
     * @return 获取到的文件信息集合
     * @throws FileNotFoundException 找不到文件异常
     */
    public List<FileInfo> getWorkflowFiles() throws FileNotFoundException {
        if (workflowFiles != null) {
            return workflowFiles;
        }
        createDir();
        workflowFiles = new ArrayList<>();
        // 获取表单pdf
        if (formPdfOptions.isCreateFormPdf()) {
            workflowFiles.add(getFormPdf());
        }
        // 获取正文和附件pdf
        List<FileInfo> mainDocAndAttachment = getMainDocAndAttachment();
        if (ArchiveConfig.getEnableQysSignatureVerify()) {
            log.info("启用契约锁验证签名");
            fileSignatureHandler.addSignature(mainDocAndAttachment, this.requestId, this.savePath);
        }else {
            log.info("不启用契约锁验证签名");
        }
        workflowFiles.addAll(mainDocAndAttachment);
        return workflowFiles;
    }

    /**
     * 获取流程正文和附件
     */
    private List<FileInfo> getMainDocAndAttachment() throws FileNotFoundException {
        List<FileInfo> fileList = new ArrayList<>();
        if (isAttachmentInMainBody) {
            // 对于附件在正文文档的附件中，获取正文pdf时取正文文档的正文，获取附件pdf时取正文文档的附件
            List<FileInfo> pdfFiles = getFilesInMainBodyDoc();
            log.info("取到的正文和附件文件：" + pdfFiles);
            fileList.addAll(pdfFiles);
        } else {
            // 取正文字段的文档pdf，取到的文件都算是正文
            List<FileInfo> mainBodyFile = getFiles(FileType.MAIN_BODY,
                    documentFieldOptions.getMainBodyFieldValue(), savePath);
            log.info("取到的正文文件：" + mainBodyFile);
            fileList.addAll(mainBodyFile);
        }
        // 从附件字段中获取附件
        List<FileInfo> imageFiles = getFiles(FileType.ATTACHMENT, documentFieldOptions.getImageFileValue(), savePath);
        log.info("取到的附件文件：" + imageFiles);
        fileList.addAll(imageFiles);
        addSuffix(fileList);
        if (documentFieldOptions.isMainDocRequired() &&
                fileList.stream().noneMatch(i -> i.getFileType() == FileType.MAIN_BODY)) {
            throw new FileNotFoundException("取不到正文文件，正文必需上传，请确认表单字段是否有值");
        }
        if (documentFieldOptions.isAttachmentRequired() &&
                fileList.stream().noneMatch(i -> i.getFileType() == FileType.ATTACHMENT)) {
            throw new FileNotFoundException("取不到附件文件，附件必需上传，请确认表单字段是否有值");
        }
        return fileList;
    }

    private List<FileInfo> getFilesInMainBodyDoc() {
        List<FileInfo> fileInfos = new ArrayList<>();
        List<DocFileInfo> pdfFiles = new ArrayList<>();
        List<Integer> mainBodyFieldValue = documentFieldOptions.getMainBodyFieldValue();
        if (CollUtil.isNotEmpty(mainBodyFieldValue)) {
            for (Integer docId : mainBodyFieldValue) {
                pdfFiles.addAll(getPdfFiles(savePath, docId));
            }
        }
        for (DocFileInfo pdfFile : pdfFiles) {
            FileType fileType = pdfFile.getFileType() == DocFileType.MAIN_BODY ? FileType.MAIN_BODY :
                    FileType.ATTACHMENT;
            FileInfo fileInfo = getFileInfo(fileType, pdfFile);
            renameFileToUuid(fileInfo);
            fileInfos.add(fileInfo);
        }
        return fileInfos;
    }

    private void addSuffix(List<FileInfo> fileInfoList) {
        for (FileInfo fileInfo : fileInfoList) {
            if (fileInfo.getFileType() == FileType.MAIN_BODY) {
                fileInfo.setTitle(addFileNameSuffix(fileInfo.getTitle(),"-正本"));
            }
            if (fileInfo.getDocId() != null) {
                boolean isHlg = CollUtil.isNotEmpty(documentFieldOptions.getHlgFieldValue())
                        && documentFieldOptions.getHlgFieldValue().contains(fileInfo.getDocId());
                if (isHlg) {
                    fileInfo.setTitle(addFileNameSuffix(fileInfo.getTitle(),"-定稿"));
                }
            }
        }
    }

    /**
     * 在文件名后面（扩展名前面）加上后缀
     */
    private String addFileNameSuffix(String fileName, String suffix) {
        String extend = com.customization.yll.common.util.FileUtil.getSuffix(fileName);
        String fileNameSub = fileName.substring(0, fileName.lastIndexOf("."));
        if (!fileNameSub.endsWith(suffix)) {
            fileNameSub += suffix;
            return fileNameSub + "." + extend;
        }
        return fileName;
    }

    public void deleteTempFile() {
        if (!FileUtil.del(savePath)) {
            log.error("删除打包生成的临时文件失败");
        }
    }

    private void createDir() {
        File saveDir = new File(savePath);
        if (saveDir.exists()) {
            FileUtil.del(saveDir);
        }
        try {
            Files.createDirectories(Paths.get(savePath));
        } catch (IOException e) {
            throw new HandleWorkflowFileException("创建归档文件夹失败,路径：" + savePath);
        }
    }

    private List<FileInfo> getFiles(FileType fileType, List<Integer> docIds, String savePath) {
        if (CollUtil.isEmpty(docIds)) {
            return new ArrayList<>();
        }
        List<FileInfo> files = new ArrayList<>();
        for (Integer id : docIds) {
            List<DocFileInfo> pdfFiles = getPdfFiles(savePath, id);
            for (DocFileInfo docFileInfo : pdfFiles) {
                FileInfo fileInfo = getFileInfo(fileType, docFileInfo);
                renameFileToUuid(fileInfo);
                files.add(fileInfo);
            }
        }
        return files;
    }

    private List<DocFileInfo> getPdfFiles(String savePath, int docId) {
        List<DocFileInfo> pdfFiles = new ArrayList<>();
        List<DocFileInfo> docFiles;
        try {
            docFiles = docFileManager.getDocFiles(docId, savePath, true);
        } catch (IOException e) {
            throw new HandleWorkflowFileException("获取流程的文档文件失败", e);
        }
        if (docFiles.isEmpty()) {
            throw new HandleWorkflowFileException("获取不到该文档中的文件，文档id：" + docId);
        }
        for (DocFileInfo docFile : docFiles) {
            String suffix = com.customization.yll.common.util.FileUtil.getSuffix(docFile.getFileName());
            if ("pdf".equalsIgnoreCase(suffix)) {
                pdfFiles.add(docFile);
            } else {
                Optional<DocFileInfo> pdfFile = convertToPdf(docFile);
                pdfFile.ifPresent(pdfFiles::add);
            }
        }
        return pdfFiles;
    }

    private void renameFileToUuid(FileInfo fileInfo) {
        File file = new File(fileInfo.getFilePath());
        String fileName = file.getName();
        String newFileName = UUID.randomUUID() +
                "." + com.customization.yll.common.util.FileUtil.getSuffix(fileName);
        String newFilePath = file.getParent() + com.customization.yll.common.util.FileUtil.getSeparator()
                + newFileName;
        log.info("重命名文件，原文件路径：" + fileInfo.getFilePath());
        FileUtil.rename(file, newFileName, false, true);
        fileInfo.setFilePath(newFilePath);
    }

    private FileInfo getFormPdf() {
        int formPdfFileId = createFormPdf();
        log.info("表单pdf文件id：" + formPdfFileId);
        String fileTitle = getFormPdfFileName(formPdfFileId, new RecordSet());
        String fileName = "formPdf-" + UUID.randomUUID() + ".pdf";
        String pdfPath = this.savePath + com.customization.yll.common.util.FileUtil.getSeparator() + fileName;
        log.info("表单pdf文件保存路径：" + pdfPath);
        try {
            // 保存pdf文件到指定路径
            DocUtil.getImageFile(formPdfFileId, pdfPath);
        } catch (IOException e) {
            throw new HandleWorkflowFileException("保存表单PDF文件失败", e);
        }
        return new FileInfo(fileTitle, FileType.APPROVAL_INFORMATION,
                formPdfFileId, formPdfFileId + "", pdfPath,null);
    }

    private Optional<DocFileInfo> convertToPdf(DocFileInfo docFile) {
        log.info("转为pdf，文件路径：" + docFile.getFilePath());
        String pdfSavePath = docFile.getFilePath().substring(0, docFile.getFilePath().lastIndexOf(".")) + ".pdf";
        String fileConvertMethod = ArchiveConfig.getFileConvertMethod();
        switch (fileConvertMethod) {
            case FileConvertMethod.WPS:
                // 使用wps集成进行转换
                if (!FileConvertUtil.convertToPdfByWpsIntegrationSave(pdfSavePath, docFile.getImageFileId())) {
                    throw new DocConvertException("使用wps集成转换为pdf失败");
                }
                break;
            case FileConvertMethod.WPS_API:
                // 使用 wps 中台api进行转换
                convertPdfByWpsApi(docFile, pdfSavePath);
                break;
            default:
                throw new ConfigurationException("文件转换方式配置不正确");
        }
        log.info("转换成功");
        String pdfFileName = docFile.getFileName().substring(0, docFile.getFileName()
                .lastIndexOf(".")) + ".pdf";
        docFile.setFilePath(pdfSavePath);
        docFile.setFileName(pdfFileName);
        return Optional.of(docFile);
    }

    @NotNull
    private FileInfo getFileInfo(FileType fileType, DocFileInfo docFileInfo) {
        String fileId;
        if (docFileInfo.isFromZip()) {
            fileId = docFileInfo.getImageFileId() + "-" + UUID.randomUUID().toString().replace("-", "");
        } else {
            fileId = String.valueOf(docFileInfo.getImageFileId());
        }
        String title = docFileInfo.getFileName();
        return new FileInfo(title, fileType, docFileInfo.getImageFileId(),
                fileId, docFileInfo.getFilePath(),docFileInfo.getDocId());
    }

    private boolean convertPdfByAspose(String sourceFile, String pdfFilePath) {
        try {
            String result = AsposePDFConverter.doc2Pdf(sourceFile, pdfFilePath);
            return !StringUtils.isEmpty(result);
        } catch (Exception e) {
            log.error("使用 Aspose 转换pdf失败", e);
            return false;
        }
    }

    private void convertPdfByWpsApi(DocFileInfo docFile, String pdfSavePath) {
        String wpsServerHost = ArchiveConfig.getWpsServerHost();
        String wpsAccessKey = ArchiveConfig.getWpsAccessKey();
        String wpsSecret = ArchiveConfig.getWpsSecret();
        if (StrUtil.isEmpty(wpsServerHost)) {
            throw new PropNotConfigureException("wps服务地址未配置");
        }
        if (StrUtil.isEmpty(wpsAccessKey)) {
            throw new PropNotConfigureException("wps AccessKey 未配置");
        }
        if (StrUtil.isEmpty(wpsSecret)) {
            throw new PropNotConfigureException("wps Secret 未配置");
        }
        String oaAddress = ArchiveConfig.getOaAddress();
        log.info("oa 地址：" + oaAddress);
        DocConvertorByWpsApi convertor = new DocConvertorByWpsApi(oaAddress,
                wpsServerHost, wpsSecret, wpsAccessKey);
        File pdf = convertor.convert(docFile.getImageFileId(), pdfSavePath, "pdf", null);
        if (!Files.exists(pdf.toPath())) {
            throw new DocConvertException("转换pdf失败，不能获取到保存的pdf文件，路径：" + pdfSavePath);
        }
    }

    private int createFormPdf() {
        RecordSet recordSet = new RecordSet();
        recordSet.executeQuery("select workflowid,currentnodeid from workflow_requestbase where requestid=?",
                requestId);
        recordSet.next();
        int workflowId = recordSet.getInt("workflowid");
        int nodeId;
        if (formPdfOptions.getFormPdfNodePositionType() == FormPdfNodePositionType.CURRENT_NODE) {
            nodeId = recordSet.getInt("currentnodeid");
        } else {
            if (formPdfOptions.getFormPdfNodeId() == null) {
                throw new ConfigurationException("请选择要生成流程表单PDF的节点id");
            }
            nodeId = formPdfOptions.getFormPdfNodeId();
        }
        WorkflowFormPdfCreator formPdfCreator = new WorkflowFormPdfCreator();
        Optional<Integer> fileIdOp = formPdfCreator.createFormPdf(workflowId, nodeId, requestId);
        if (!fileIdOp.isPresent()) {
            throw new HandleWorkflowFileException("获取不到表单PDF文件，请确认流程是否开启流程存为文档并且勾选表单PDF");
        }
        return fileIdOp.get();
    }

    private String getFormPdfFileName(int fileId, RecordSet recordSet) {
        recordSet.executeQuery("select imagefilename from imagefile where imagefileid=?", fileId);
        recordSet.next();
        return recordSet.getString("imagefilename");
    }

}
