package com.customization.yll.wuling.archive.file;

import com.customization.yll.common.doc.bean.DocFileInfo;

import java.io.File;

/**
 * @author 姚礼林
 * @desc 文件转换器
 * @date 2026/9/4
 **/
public interface FileConvertor {
    /**
     * 文档格式转换
     *
     * @param docFile 文档信息
     * @return 转换后文件
     */
    File convert(DocFileInfo docFile);
}
