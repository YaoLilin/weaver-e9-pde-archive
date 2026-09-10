package com.customization.yll.wuling.archive.file;

import cn.hutool.core.util.StrUtil;
import com.customization.yll.common.doc.DocConvertorByWpsApi;
import com.customization.yll.common.doc.bean.DocFileInfo;
import com.customization.yll.common.doc.util.FileConvertUtil;
import com.customization.yll.common.exception.ConfigurationException;
import com.customization.yll.common.exception.DocConvertException;
import com.customization.yll.common.exception.PropNotConfigureException;
import com.customization.yll.wuling.archive.config.ArchiveConfig;
import com.customization.yll.wuling.archive.constants.FileConvertMethod;
import com.customization.yll.wuling.archive.constants.YozoDcsConvertType;
import weaver.integration.logging.Logger;
import weaver.integration.logging.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.util.Collections;

/**
 * @author 姚礼林
 * @desc 档案文件格式转换器
 * @date 2026/9/4
 **/
public class ArchiveFileConvertor implements FileConvertor {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final YozoDcsConvertManager yozoDcsConvertManager;

    public ArchiveFileConvertor(YozoDcsConvertManager yozoDcsConvertManager) {
        this.yozoDcsConvertManager = yozoDcsConvertManager;
    }

    /**
     * 将文档文件转换为 PDF。
     *
     * @param docFile 待转换的文档文件信息
     * @return 已生成的 PDF 文件
     */
    @Override
    public File convert(DocFileInfo docFile) {
        log.info("转为 PDF，文件路径：" + docFile.getFilePath());
        String pdfSavePath = docFile.getFilePath().substring(0, docFile.getFilePath().lastIndexOf(".")) + ".pdf";
        String fileConvertMethod = ArchiveConfig.getFileConvertMethod();
        if (FileConvertMethod.WPS.equals(fileConvertMethod)) {
            convertByWpsIntegration(docFile, pdfSavePath);
        } else if (FileConvertMethod.WPS_API.equals(fileConvertMethod)) {
            convertByWpsApi(docFile, pdfSavePath);
        } else if (FileConvertMethod.YOZO_DCS.equals(fileConvertMethod)) {
            convertByYozoDcs(docFile, pdfSavePath);
        } else {
            throw new ConfigurationException("文件转换方式配置不正确");
        }
        File pdfFile = new File(pdfSavePath);
        if (!pdfFile.isFile()) {
            throw new DocConvertException("转换 PDF 失败，不能获取到保存的 PDF 文件，路径：" + pdfSavePath);
        }
        log.info("转换 PDF 成功，文件路径：" + pdfSavePath);
        return pdfFile;
    }

    private void convertByWpsIntegration(DocFileInfo docFile, String pdfSavePath) {
        if (!FileConvertUtil.convertToPdfByWpsIntegrationSave(pdfSavePath, docFile.getImageFileId())) {
            throw new DocConvertException("使用 WPS 集成转换为 PDF 失败");
        }
    }

    private void convertByWpsApi(DocFileInfo docFile, String pdfSavePath) {
        String wpsServerHost = ArchiveConfig.getWpsServerHost();
        String wpsAccessKey = ArchiveConfig.getWpsAccessKey();
        String wpsSecret = ArchiveConfig.getWpsSecret();
        if (StrUtil.isBlank(wpsServerHost)) {
            throw new PropNotConfigureException("wps服务地址未配置");
        }
        if (StrUtil.isBlank(wpsAccessKey)) {
            throw new PropNotConfigureException("wps AccessKey 未配置");
        }
        if (StrUtil.isBlank(wpsSecret)) {
            throw new PropNotConfigureException("wps Secret 未配置");
        }
        String oaAddress = ArchiveConfig.getOaAddress();
        log.info("oa 地址：" + oaAddress);
        DocConvertorByWpsApi convertor = new DocConvertorByWpsApi(oaAddress,
                wpsServerHost, wpsSecret, wpsAccessKey);
        File pdf = convertor.convert(docFile.getImageFileId(), pdfSavePath, "pdf", null);
        if (pdf == null || !Files.exists(pdf.toPath())) {
            throw new DocConvertException("转换 PDF 失败，不能获取到保存的 PDF 文件，路径：" + pdfSavePath);
        }
    }

    /**
     * 调用私有部署的永中 DCS {@code /composite/upload} 接口，将本地 Office 文件转换为 PDF。
     *
     * @param docFile     待转换文档文件信息
     * @param pdfSavePath 转换后 PDF 的本地保存路径
     */
    private void convertByYozoDcs(DocFileInfo docFile, String pdfSavePath) {
        YozoDcsConvertParam param = new YozoDcsConvertParam(
                ArchiveConfig.getYozoDcsServerAddress(), YozoDcsConvertType.OFFICE_TO_PDF,
                Collections.<String, String>emptyMap(), Collections.<String, String>emptyMap());
        yozoDcsConvertManager.convert(new File(docFile.getFilePath()), pdfSavePath, param);
    }
}
