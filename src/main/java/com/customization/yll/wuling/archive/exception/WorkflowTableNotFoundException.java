package com.customization.yll.wuling.archive.exception;

/**
 * 未找到流程表名异常
 * @author yaolilin
 */
public class WorkflowTableNotFoundException extends RuntimeException{
    public WorkflowTableNotFoundException() {
    }

    public WorkflowTableNotFoundException(String message) {
        super(message);
    }
}
