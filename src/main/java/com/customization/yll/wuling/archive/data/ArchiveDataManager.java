package com.customization.yll.wuling.archive.data;

import cn.hutool.core.collection.CollUtil;
import com.customization.yll.common.enu.LanguageType;
import com.customization.yll.common.exception.ConfigModeDataNotFoundException;
import com.customization.yll.common.util.HrmInfoUtil;
import com.customization.yll.common.util.MultiLanguageUtil;
import com.customization.yll.common.workflow.WorkflowApprovalInfoManager;
import com.customization.yll.common.workflow.WorkflowFieldValueManager;
import com.customization.yll.common.workflow.entity.WorkflowApprovalInfoEntity;
import com.customization.yll.wuling.archive.config.ConfigurationModeDataManager;
import com.customization.yll.wuling.archive.constants.FileType;
import com.customization.yll.wuling.archive.entity.DocumentFieldOption;
import com.customization.yll.wuling.archive.entity.ModeConfMainFieldEntity;
import com.customization.yll.wuling.archive.exception.ArchiveDataException;
import com.customization.yll.wuling.archive.exception.WorkflowTableNotFoundException;
import com.customization.yll.wuling.archive.file.FileInfo;
import com.customization.yll.wuling.archive.file.FileSignatureInfo;
import com.customization.yll.wuling.archive.file.FormPdfOptions;
import com.customization.yll.wuling.archive.file.WorkflowFileManager;
import com.customization.yll.wuling.archive.util.CommonArchiveDataUtil;
import com.pde.pdes.eep.domian.metadata.*;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.jetbrains.annotations.NotNull;
import weaver.conn.RecordSet;
import weaver.general.GCONST;
import weaver.integration.logging.Logger;
import weaver.integration.logging.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 获取归档数据，用于推送到档案系统，包含文件实体元数据、业务实体元数据等
 *
 * @author yaolilin
 */
public class ArchiveDataManager {
    private final RecordSet recordSet = new RecordSet();
    @Setter
    private AbstractFileMetadataManager metadataManager;
    @Setter
    private WorkflowFileManager fileManager;
    @Setter
    private BusinessMetadataManager businessMetadataManager;
    private final Integer requestId;
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private Map<String, String> entry;
    private List<Doc> documents;
    private List<AgentEntity> agentEntities;


    public ArchiveDataManager(Integer requestId, Integer configId)
            throws ConfigModeDataNotFoundException, WorkflowTableNotFoundException {
        this.requestId = requestId;
        ConfigurationModeDataManager modeDataManager = new ConfigurationModeDataManager(configId);
        WorkflowFieldValueManager fieldValueManager = new WorkflowFieldValueManager(requestId);
        this.metadataManager = new FileMetadataManager(modeDataManager, fieldValueManager, requestId);
        this.businessMetadataManager = new BusinessMetadataManager(modeDataManager, requestId);
        this.fileManager = getWorkflowFileManager(requestId, modeDataManager, fieldValueManager);
    }

    @NotNull
    private WorkflowFileManager getWorkflowFileManager(Integer requestId,
                                                       ConfigurationModeDataManager modeDataManager,
                                                       WorkflowFieldValueManager fieldValueManager) {
        String savePath = GCONST.getRootPath() + "filesystem/archive/temp/workflowFile/" +
                requestId + "_" + getCurrentDate();
        ModeConfMainFieldEntity mainFieldEntity = modeDataManager.getMainFieldEntity();
        DocumentFieldOption documentFieldOption = getDocumentFieldValues(fieldValueManager, mainFieldEntity);
        FormPdfOptions formPdfOptions = new FormPdfOptions(mainFieldEntity.isCreateFormPdf(),
                mainFieldEntity.getFormPdfNodePositionType(), mainFieldEntity.getFormPdfNodeId());
        return new WorkflowFileManager(requestId,
                mainFieldEntity.isAttachmentInMainBody(), documentFieldOption, formPdfOptions, savePath);
    }

    @NotNull
    private DocumentFieldOption getDocumentFieldValues(WorkflowFieldValueManager fieldValueManager,
                                                       ModeConfMainFieldEntity mainFieldEntity) {
        DocumentFieldOption documentFieldValues = new DocumentFieldOption();
        if (mainFieldEntity.getMainBody() != null) {
            String workflowMainBodyValue = fieldValueManager.getFieldValue(mainFieldEntity.getMainBody());
            documentFieldValues.setMainBodyFieldValue(getFieldDocIds(workflowMainBodyValue));
        }
        List<Integer> imageFiles = new ArrayList<>();
        if (mainFieldEntity.getImageFile() != null) {
            String workflowImageFileValue = fieldValueManager.getFieldValue(mainFieldEntity.getImageFile());
            imageFiles.addAll(getFieldDocIds(workflowImageFileValue));
        }
        // 获取其它附件文档id，其它附件可以配置多个流程字段，要对每个字段分别获取文档id
        if (CollUtil.isNotEmpty(mainFieldEntity.getOtherImageFiles())) {
            for (Integer fieldId : mainFieldEntity.getOtherImageFiles()) {
                String fieldValue = fieldValueManager.getFieldValue(fieldId);
                imageFiles.addAll(getFieldDocIds(fieldValue));
            }
        }
        // 获取流程花脸稿字段值
        List<Integer> hlgDocs = new ArrayList<>();
        if (CollUtil.isNotEmpty(mainFieldEntity.getHlgDocs())) {
            for (Integer fieldId : mainFieldEntity.getHlgDocs()) {
                String fieldValue = fieldValueManager.getFieldValue(fieldId);
                hlgDocs.addAll(getFieldDocIds(fieldValue));
            }
        }
        documentFieldValues.setImageFileValue(imageFiles);
        documentFieldValues.setHlgFieldValue(hlgDocs);
        documentFieldValues.setMainDocRequired(mainFieldEntity.isMainDocRequired());
        documentFieldValues.setAttachmentRequired(mainFieldEntity.isAttachmentRequired());
        return documentFieldValues;
    }

    private List<Integer> getFieldDocIds(String fieldValue) {
        if (!fieldValue.isEmpty()) {
            return Arrays.stream(fieldValue.split(","))
                    .map(Integer::parseInt).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    /**
     * 此处封装文件实体元数据相关信息
     *
     * @return map
     */
    public Map<String, String> getEntry() throws FileNotFoundException {
        if (entry != null) {
            return entry;
        }
        this.entry = metadataManager.getMetadata();
        List<FileInfo> workflowFiles = fileManager.getWorkflowFiles();
        entry.put("页数", String.valueOf(getDocPages(workflowFiles)));
        log.info("文件实体元数据：" + entry);
        return entry;
    }

    /**
     * 此处封装原文，所有文档信息集合
     *
     * @return List<Doc>
     */
    public List<Doc> getDocuments() throws FileNotFoundException {
        if (documents != null) {
            return documents;
        }
        // 电子文档列表，需要把一份文件的所有文档列进如，如正文、附件、文单、定稿、关联参阅件等等
        // 公文归档：1.归档电子文件一般以每份文件为一件；2.正文、附件为一件；3.如果正文发布有审批流程，则审批流程应转换为PDF文件，
        // 文件类型为附件一并归档；4.来文与复文一般独立成件；5.同一文件编号的同一版本原始格式和转换格式为一件；
        // 6.同一文件编号的不同版本独立成件；7.同一文件编号的同一版本的不同语种版本一般独立成件；8.同一文件编号的更改文件一般独立成件
        // 格式要求：公文统一为PDF格式文件；公文如果经过电子签章或数字签名，应做图形化转换。
        List<FileInfo> allFiles = fileManager.getWorkflowFiles();
        allFiles.sort(Comparator.comparingInt(i -> i.getFileType().getOrder()));
        log.info("文件信息：" + allFiles);
        documents = new ArrayList<>();
        int order = 1;
        for (FileInfo fileInfo : allFiles) {
            Doc doc = getDoc(fileInfo, order++);
            documents.add(doc);
        }
        return documents;
    }

    /**
     * 获取业务实体元数据
     *
     * @return 业务实体元数据
     */
    public List<BusinessEntity> getBusinessEntities() {
        List<BusinessEntity> businessEntities = businessMetadataManager.getBusinessEntities();
        log.info("业务实体元数据:" + businessEntities);
        return businessEntities;
    }

    /**
     * 机构人员实体
     *
     * @return List<AgentEntity>
     */
    public List<AgentEntity> getAgentEntities() {
        if (this.agentEntities != null) {
            return agentEntities;
        }
        this.agentEntities = new ArrayList<>();
        WorkflowApprovalInfoManager approvalInfoManager = new WorkflowApprovalInfoManager();
        List<WorkflowApprovalInfoEntity> approvalInfoList = approvalInfoManager.getWorkflowApprovalInfo(requestId);
        Set<Integer> approveOperators = new HashSet<>();
        for (WorkflowApprovalInfoEntity approvalInfo : approvalInfoList) {
            if (approveOperators.contains(approvalInfo.getOperator())) {
                continue;
            }
            approveOperators.add(approvalInfo.getOperator());
            String lastName = MultiLanguageUtil.analyzeMultiLanguageText(
                    HrmInfoUtil.getLastName(approvalInfo.getOperator(), recordSet), LanguageType.CN, recordSet);
            String postName = HrmInfoUtil.getPostName(approvalInfo.getOperator(), recordSet);
            String workCodeOrId = HrmInfoUtil.getWorkCode(approvalInfo.getOperator(), recordSet);
            if (StringUtils.isEmpty(workCodeOrId)) {
                workCodeOrId = approvalInfo.getOperator() + "";
            }
            AgentEntity agent = getAgentEntity(workCodeOrId, lastName, postName);
            this.agentEntities.add(agent);
        }
        log.info("机构人员实体:" + agentEntities);
        return this.agentEntities;
    }

    /**
     * 文件实体关系
     */
    public List<RecordRelation> getRecordRelations() throws FileNotFoundException {
        List<FileInfo> files = fileManager.getWorkflowFiles();
        List<RecordRelation> relations = new ArrayList<>();
        Optional<FileInfo> mainBody = files.stream().filter(i -> i.getFileType() == FileType.MAIN_BODY).findAny();
        for (FileInfo file : files) {
            RecordRelation relation = new RecordRelation();
            // 文件标识符：即文件实体元数据中的业务系统条目数据唯一ID
            relation.setId(file.getId());
            if (file.getFileType() == FileType.MAIN_BODY) {
                // 被关联文件标识符
                relation.setRelatedRecordId(file.getId());
                // 关系:电子文件之间、电子文件不同实体之间以及电子文件实体内部对象之间的相互关系
                // 关系的值域：转发/被转发，来文/复文，正文/附件，新版本/旧版本，包含/被包含，前/后，替代/被替代，参考/被参考，参见/被参件，
                // 引用/被引用，操控/被操控，完成/被完成，形成/被形成，隶属/被隶属
                relation.setRelation("正文");
            }else {
                if (mainBody.isPresent()) {
                    relation.setRelatedRecordId(mainBody.get().getId());
                }else {
                    relation.setRelatedRecordId(file.getId());
                }
                relation.setRelation("附件");
            }
            //关系描述：对关系类型和关系的进一步说明
            relation.setRelationMemo("原文与条目的关系");
            relation.setRelationType("文档-文件");
            relations.add(relation);
        }
        return relations;
    }

    private int getDocPages(List<FileInfo> docs) {
        int pages = 0;
        for (FileInfo doc : docs) {
            try (PDDocument document = PDDocument.load(new File(doc.getFilePath()))) {
                pages += document.getNumberOfPages();
            } catch (Exception e) {
                throw new ArchiveDataException("获取文件页数失败，文件路径：" + doc.getFilePath(), e);
            }
        }
        return pages;
    }

    private @NotNull AgentEntity getAgentEntity(String workCodeOrId, String lastName, String postName) {
        AgentEntity agent = new AgentEntity();
        //机构人员标识符:业务系统赋予机构人员名称的唯一标识
        agent.setId(workCodeOrId);
        //机构人员类型:形成、处理和管理电子文件的机构/人员的类别。在形成、处理、管理电子文件的系统中通过预定义值域列表选择著录或手工著录。
        //机构人员类型值域：当流程节点参与人为单位共用账号，著录“单位”；当流程节点参与人为部门共用账号，著录“内设机构”；当流程节点参与人已经明确到具体操作人时，著录“个人”。业务系统通常流程节点已经明确到具体操作人，故通常为“个人”
        agent.setType("个人");
        //机构人员名称:形成、处理和管理电子文件的机构/人员称谓。著录全称或规范化的简称。当机构人员类型（M76)的值为“单位”时，著录单位名称；当机构人员类型（M76)的值为“内设机构”时，著录内设机构名称；当机构人员类型（M76)的值为“个人”时，著录个人名称
        agent.setName(lastName);
        //组织机构代码:由国家组织机构代码管理中心为在中华人民共和国境内依法成立的机关、企业、事业单位、社会团体和民办非企业单位等机构赋予的一个全国范围内唯一的、始终不变的法定代码标识
        agent.setCode("91450200198605397G");
        //个人职位:履行电子文件形成、处理、管理等具体业务行为的个人的职位
        agent.setPosts(postName);
        return agent;
    }

    public void deleteTempFile() {
        fileManager.deleteTempFile();
    }

    private String getCurrentDate() {
        return CommonArchiveDataUtil.getCurrentDate();
    }

    private Doc getDoc(FileInfo fileInfo, int order) {
        File file = new File(fileInfo.getFilePath());
        Doc doc = new Doc();
        //文档路径，这里目前用的文档绝对路径，也就是打包时文档要落地的绝对路径
        doc.setPath(file.getAbsolutePath());
        //文档标识符，规则可自定义
        doc.setDocId(fileInfo.getId());
        doc.setDocTitle(fileInfo.getTitle());
        //文档的序号，如果有严格的序号规则，需要严格填写，本示例暂用循环的序号
        doc.setSeq(order + "");
        //计算机文件名
        doc.setDocName(file.getName());
        //电子原文的类型，例如：正文、附件、文单、定稿、关联参阅件
        doc.setDocType(fileInfo.getFileType().getName());
        //计算机文件大小：电子原文的文件大小可以通过文件获取，或者从系统中存储的文件大小插入
        doc.setDocSize(String.valueOf(file.length()));
        //文档创建程序:形成和处理文档的程序名称和版本，例Microsoft Office Word
        doc.setDocCreateApp("");
        //信息系统描述:生成或管理带电子文件的信息系统的描述信息。著录信息系统名称、版本、功能、开发商名称
        doc.setSystemInfo("");
        //格式名称,这里用的代码去获取文件后缀名称
        doc.setSuffix(file.getName().substring(file.getName().lastIndexOf(".") + 1));
        //格式版本,文档的版本号
        doc.setVersion("1.0");
        //格式描述,录音录像类、照片类电子档案编码格式的一组描述信息。例如MP3是xxxx的简称，是MPEG Layer 3标准压缩编码的一种音频格式文件；示例 JPG
        doc.setSuffixMemo("");
        //数字化对象形态:被数字化文件或档案的载体类型、物理尺寸等信息的描述。著录文件或档案载体的类型及尺寸，例如：缩微卷片，35mm扫描影像，A4
        //doc.setDigitizedObject("数字化对象形态");
        //扫描分辨率：文件或档案被数字化时，相关数字化设备所采用的取样分辨率，即单位长度内的取样点数，一般用每英寸点数(dpi)表示。本元素的值由数量和单位两部分组成，例如：300dpi
        doc.setResolution("");
        //扫描色彩模式：文件或档案被数字化时，相关数字化设备所采用的扫描色彩模式。扫描色彩模式的选择应符合DA/T 31-2005的有关规定。
        doc.setColorModel("");
        //图像压缩方案：文件或档案数字化生成数字图像时所采用的压缩方案，可著录图像压缩方案名称，例如：CCITT Group 4
        doc.setCompressionScheme("");
        //扫描仪生产商：
        doc.setScannerManufacturer("");
        //扫描仪型号：
        doc.setScannerModel("");
        if (fileInfo.isSignatureFile()) {
            addDocSignature(doc, fileInfo);
        }
        return doc;
    }

    private void addDocSignature(Doc doc, FileInfo fileInfo) {
        if (CollUtil.isEmpty(fileInfo.getSignatureInfo())) {
            throw new ArchiveDataException("文件无签名信息");
        }
        List<DigitalSignature> signatureList = new ArrayList<>();
        for (FileSignatureInfo signatureInfo : fileInfo.getSignatureInfo()) {
            DigitalSignature signature = getDigitalSignature(signatureInfo);
            signatureList.add(signature);
        }
        doc.setSignatures(signatureList);
    }

    @NotNull
    private static DigitalSignature getDigitalSignature(FileSignatureInfo signatureInfo) {
        BaseSignature.Block block = new BaseSignature.Block();
        block.setCertificate(signatureInfo.getOrganization());
        block.setReference( signatureInfo.getCertSerialNo());

        DigitalSignature signature = new DigitalSignature();
        signature.setRule("契约锁");
        signature.setTime(signatureInfo.getTime());
        signature.setSinger(signatureInfo.getOperator());
        signature.setSignature(signatureInfo.getSignReason());
        signature.setBlock(block);
        signature.setAlgorithm(signatureInfo.getStrAlgName());
        return signature;
    }
}
