package com.customization.yll.wuling.archive.exception;

/**
 * 封装归档信息包异常
 * @author yaolilin
 */
public class ArchivePackException extends RuntimeException{
    public ArchivePackException() {
    }

    public ArchivePackException(String message) {
        super(message);
    }

    public ArchivePackException(String message, Throwable cause) {
        super(message, cause);
    }
}
