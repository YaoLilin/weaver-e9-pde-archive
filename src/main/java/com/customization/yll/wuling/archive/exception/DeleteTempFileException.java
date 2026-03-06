package com.customization.yll.wuling.archive.exception;

/**
 * 删除临时文件异常
 * @author yaolilin
 */
public class DeleteTempFileException extends Exception{
    public DeleteTempFileException() {
    }

    public DeleteTempFileException(String message) {
        super(message);
    }

    public DeleteTempFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
