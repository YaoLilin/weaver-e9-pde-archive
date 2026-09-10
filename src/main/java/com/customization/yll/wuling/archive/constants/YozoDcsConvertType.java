package com.customization.yll.wuling.archive.constants;

import lombok.Getter;

/**
 * 永中 DCS 文档转换类型。
 *
 * @author 姚礼林
 * @date 2026/9/5
 */
@Getter
public enum YozoDcsConvertType {
    /** Office 文档转换为高清 HTML 页面。 */
    OFFICE_TO_HIGH_DEFINITION_HTML(0, "Office 文档转换为高清 HTML 页面"),
    /** Office 文档转换为 TXT 文件。 */
    OFFICE_TO_TEXT(2, "Office 文档转换为 TXT 文件"),
    /** Office 文档转换为 PDF 文件。 */
    OFFICE_TO_PDF(3, "Office 文档转换为 PDF 文件"),
    /** Office 文档转换为图片，支持 GIF、PNG、JPG、TIFF、BMP。 */
    OFFICE_TO_IMAGE(5, "Office 文档转换为图片，支持 GIF、PNG、JPG、TIFF、BMP"),
    /** PDF 文档转换为图片，支持 GIF、PNG、JPG、TIFF、BMP。 */
    PDF_TO_IMAGE(10, "PDF 文档转换为图片，支持 GIF、PNG、JPG、TIFF、BMP"),
    /** PDF 文档转换为 HTML 页面。 */
    PDF_TO_HTML(14, "PDF 文档转换为 HTML 页面"),
    /** 压缩文件转换为 HTML 页面，支持 ZIP、RAR、7Z、TAR、GZ。 */
    ARCHIVE_TO_HTML(19, "压缩文件转换为 HTML 页面，支持 ZIP、RAR、7Z、TAR、GZ"),
    /** PDF 文件转换为 HTML 页面。 */
    PDF_TO_HTML_PAGE(20, "PDF 文件转换为 HTML 页面"),
    /** 合并多个同类型 Office 文档。 */
    MERGE_OFFICE_DOCUMENTS(22, "合并多个同类型 Office 文档"),
    /** 图片转换为 HTML 页面，支持 PNG、JPG、GIF、BMP、SVG、JPEG。 */
    IMAGE_TO_HTML(23, "图片转换为 HTML 页面，支持 PNG、JPG、GIF、BMP、SVG、JPEG"),
    /** PDF 文档转换为 TXT 文件。 */
    PDF_TO_TEXT(24, "PDF 文档转换为 TXT 文件"),
    /** 获取 Office 文档页码。 */
    GET_OFFICE_PAGE_COUNT(27, "获取 Office 文档页码"),
    /** 获取 PDF 文档页码。 */
    GET_PDF_PAGE_COUNT(28, "获取 PDF 文档页码"),
    /** Office 文档转换为 HTML 页面。 */
    OFFICE_TO_HTML(30, "Office 文档转换为 HTML 页面"),
    /** 合并多个 PDF 文档。 */
    MERGE_PDF_DOCUMENTS(31, "合并多个 PDF 文档"),
    /** 图片转换为 PDF，支持 PNG、JPG、JPEG、BMP、TIFF、SVG。 */
    IMAGE_TO_PDF(32, "图片转换为 PDF，支持 PNG、JPG、JPEG、BMP、TIFF、SVG"),
    /** Office 文档添加水印和书签后输出 Office 文档。 */
    OFFICE_ADD_WATERMARK_AND_BOOKMARK(33, "Office 文档添加水印和书签后输出 Office 文档"),
    /** PDF 文件添加水印后输出 PDF 文件。 */
    PDF_ADD_WATERMARK(34, "PDF 文件添加水印后输出 PDF 文件"),
    /** Office 文档转换为 UOF 文件。 */
    OFFICE_TO_UOF(43, "Office 文档转换为 UOF 文件"),
    /** 图片添加水印并转换为指定后缀图片。 */
    IMAGE_ADD_WATERMARK_AND_CONVERT(44, "图片添加水印并转换为指定后缀图片"),
    /** 获取 Word 文档的全部书签信息。 */
    GET_WORD_BOOKMARKS(54, "获取 Word 文档的全部书签信息"),
    /** Office 或文本文件转换为高清 HTML Canvas 页面。 */
    DOCUMENT_TO_HIGH_DEFINITION_HTML_CANVAS(61, "Office 或文本文件转换为高清 HTML Canvas 页面"),
    /** PDF 生成双层 PDF。 */
    PDF_TO_DOUBLE_LAYER_PDF(63, "PDF 生成双层 PDF"),
    /** Office 文档转换为长图，支持 JPG、PNG、BMP、TIF、GIF。 */
    OFFICE_TO_LONG_IMAGE(69, "Office 文档转换为长图，支持 JPG、PNG、BMP、TIF、GIF"),
    /** 删除 PDF 的指定页面。 */
    PDF_DELETE_PAGE(71, "删除 PDF 的指定页面"),
    /** 提取 PDF 中的图片。 */
    PDF_EXTRACT_IMAGES(72, "提取 PDF 中的图片"),
    /** 压缩 PDF 文件。 */
    PDF_COMPRESS(73, "压缩 PDF 文件"),
    /** 旋转 PDF 文件。 */
    PDF_ROTATE(74, "旋转 PDF 文件"),
    /** 为 PDF 添加页码。 */
    PDF_ADD_PAGE_NUMBER(75, "为 PDF 添加页码"),
    /** 在 PDF 中插入页面。 */
    PDF_INSERT_PAGE(76, "在 PDF 中插入页面"),
    /** PDF 转换为长图，支持 JPG、PNG。 */
    PDF_TO_LONG_IMAGE(77, "PDF 转换为长图，支持 JPG、PNG"),
    /** 解密 PDF 文件。 */
    PDF_DECRYPT(78, "解密 PDF 文件"),
    /** 加密 PDF 文件。 */
    PDF_ENCRYPT(79, "加密 PDF 文件"),
    /** 拆分 PDF 文件。 */
    PDF_SPLIT(82, "拆分 PDF 文件"),
    /** OCR 图片转换为 Word，支持 PNG、JPG、JPEG、BMP。 */
    OCR_IMAGE_TO_WORD(84, "OCR 图片转换为 Word，支持 PNG、JPG、JPEG、BMP"),
    /** 提取 Office 文档中的图片。 */
    OFFICE_EXTRACT_IMAGES(85, "提取 Office 文档中的图片"),
    /** HTML 页面原样预览。 */
    HTML_ORIGINAL_PREVIEW(500, "HTML 页面原样预览"),
    /** CAD 文件通过浩辰转换预览，支持 DWG、DWT、DXF。 */
    CAD_TO_PREVIEW(680, "CAD 文件通过浩辰转换预览，支持 DWG、DWT、DXF");

    private final int code;
    private final String description;

    YozoDcsConvertType(int code, String description) {
        this.code = code;
        this.description = description;
    }
}
