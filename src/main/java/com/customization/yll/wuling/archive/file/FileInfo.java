package com.customization.yll.wuling.archive.file;

import com.customization.yll.wuling.archive.constants.FileType;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * 文件信息
 * @author yaolilin
 */
@Data
@ToString
public class FileInfo {
    private String title;
    private FileType fileType;
    private Integer imageFileId;
    /**
     * 文件在系统中的唯一标识
     */
    private String id;
    private String filePath;
    private Integer docId;
    private boolean isSignatureFile;
    private List<FileSignatureInfo> signatureInfo;

    public FileInfo(String title, FileType fileType, Integer imageFileId, String id, String filePath, Integer docId) {
        this.title = title;
        this.fileType = fileType;
        this.imageFileId = imageFileId;
        this.id = id;
        this.filePath = filePath;
        this.docId = docId;
    }

}
