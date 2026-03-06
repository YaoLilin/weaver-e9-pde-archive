package com.customization.yll.wuling.archive.exception;

/**
 * @author 姚礼林
 * @desc 记录归档状态异常，比如将档案归档状态记录到建模时出错
 * @date 2025/10/15
 **/
public class ArchiveStatusRecordException extends RuntimeException {
    public ArchiveStatusRecordException(String message) {
        super(message);
    }

    public ArchiveStatusRecordException(String message, Throwable cause) {
        super(message, cause);
    }
}
