package com.customization.yll.wuling.archive.exception;

/**
 * @author yaolilin
 * @desc 归档数据异常
 * @date 2024/8/28
 **/
public class ArchiveDataException extends RuntimeException{
    public ArchiveDataException() {
    }

    public ArchiveDataException(String message) {
        super(message);
    }

    public ArchiveDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
