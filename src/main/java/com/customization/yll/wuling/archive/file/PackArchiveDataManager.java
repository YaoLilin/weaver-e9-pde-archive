package com.customization.yll.wuling.archive.file;

import cn.hutool.core.io.FileUtil;
import com.customization.yll.wuling.archive.config.ArchiveConfig;
import com.customization.yll.wuling.archive.data.ArchiveDataManager;
import com.customization.yll.wuling.archive.exception.ArchivePackException;
import com.customization.yll.wuling.archive.util.CommonArchiveDataUtil;
import com.pde.pdes.eep.OverrideZipRecordEepPackager;
import com.pde.pdes.eep.domain.BasicProperties;
import com.pde.pdes.eep.domain.RecordMetadataProperties;
import com.pde.pdes.eep.dto.PackageInfo;
import com.pde.pdes.eep.enums.EepTypeEnum;
import weaver.general.GCONST;
import weaver.integration.logging.Logger;
import weaver.integration.logging.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * @author yaolilin
 * @date 2024-07-29 16:32:32
 * @desc 封装归档信息包
 */
public class PackArchiveDataManager {
    private static final String PACKAGE_DIR = GCONST.getRootPath() + "filesystem/archive/temp/package/";
    private final Logger logger = LoggerFactory.getLogger(PackArchiveDataManager.class);
    /**
     * eep包临时文件路径
     */
    private final String  packagePath;
    private final Integer requestId;
    private final ArchiveDataManager metadataManager;

    public PackArchiveDataManager(ArchiveDataManager metadataManager,Integer requestId) {
        packagePath = PACKAGE_DIR + requestId + "_" + getCurrentDate();
        this.metadataManager = metadataManager;
        this.requestId = requestId;
    }

    /**
     * 数据封装打包
     *
     * @return 生成的封装包路径
     */
    public String pack(){
        try {
            String path = packagePath + "/files";
            File tempFile = new File(path);
            Files.createDirectories(Paths.get(path));
            PackageInfo info = new PackageInfo();
            //生成单位名称，
            info.setTransferUnit(ArchiveConfig.getCompanyName());
            //系统名称
            info.setSystemName(ArchiveConfig.getSourceSystemCode());
            //载体编号 业务系统归档暂时写空
            info.setMediumNum("");
            //载体类型 业务系统归档暂时写空
            info.setMediumType("");
            BasicProperties properties = BasicProperties.defaultProperties(tempFile, EepTypeEnum.FILING)
                    .setCharset(Charset.forName("GBK"))
                    //签名元数据xml
                    .setSignatureXml(true)
                    //签名文档
                    .setSignatureDoc(true)
                    //eep包说明
                    .setPackageInfo(info);
            OverrideZipRecordEepPackager packager = new OverrideZipRecordEepPackager(properties);
            properties.setNamingMaker(parent -> new File(parent, String.valueOf(UUID.randomUUID())));
            // MetadataProperties参数说明见：参数类说明章节
            packager.doPack(metadataProperties());

            // 执行打包
            File file = packager.doZip(requestId + "-" + System.currentTimeMillis()+".asip");
            logger.info(file.getAbsolutePath() + "：（" + file.length() + ")");
            return file.getAbsolutePath();
        } catch (Exception e) {
            throw new ArchivePackException("打包归档文件出错", e);
        }
    }

    public void deleteTempFiles() {
        metadataManager.deleteTempFile();
        if (!FileUtil.del(packagePath)) {
            logger.error("删除封装档案信息包临时文件出错");
        }
    }

    /**
     * 传输到档案系统的归档数据
     * @return 归档数据
     */
    private  RecordMetadataProperties metadataProperties() throws FileNotFoundException {
        RecordMetadataProperties properties = new RecordMetadataProperties();
        //重要：条目信息
        properties.setEntry(metadataManager.getEntry());
        //重要：原文信息
        properties.setDocuments(metadataManager.getDocuments());
        //重要：业务实体
        properties.setBusinessEntities(metadataManager.getBusinessEntities());
        //重要：机构人员
        properties.setAgentEntity(metadataManager.getAgentEntities());
        // 文件实体关系
        properties.setRecordRelations(metadataManager.getRecordRelations());
        return properties;
    }

    private String getCurrentDate() {
        return CommonArchiveDataUtil.getCurrentDate();
    }



}
