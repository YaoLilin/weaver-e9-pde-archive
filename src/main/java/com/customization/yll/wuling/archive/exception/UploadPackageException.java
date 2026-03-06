package com.customization.yll.wuling.archive.exception;


/**
 * 上传档案信息包异常
 * @author yaolilin
 */
public class UploadPackageException extends Exception{
    public UploadPackageException() {
    }

    public UploadPackageException(String message) {
        super(message);
    }

    public UploadPackageException(String message, Throwable cause) {
        super(message, cause);
    }
}
