package com.customization.yll.wuling.archive.exception;

/**
 * 处理流程文件异常，例如文件转换异常，创建目录异常
 * @author yaolilin
 */
public class HandleWorkflowFileException extends RuntimeException{
    public HandleWorkflowFileException() {
    }

    public HandleWorkflowFileException(String message) {
        super(message);
    }

    public HandleWorkflowFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
