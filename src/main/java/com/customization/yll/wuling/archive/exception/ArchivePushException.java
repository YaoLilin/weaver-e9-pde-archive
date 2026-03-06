package com.customization.yll.wuling.archive.exception;

/**
 * 推送档案数据异常
 * @author yaolilin
 */
public class ArchivePushException extends RuntimeException{
    public ArchivePushException() {
    }

    public ArchivePushException(String message) {
        super(message);
    }

    public ArchivePushException(String message, Throwable cause) {
        super(message, cause);
    }
}
