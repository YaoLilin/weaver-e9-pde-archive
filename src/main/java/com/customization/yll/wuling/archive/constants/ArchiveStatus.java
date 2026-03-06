package com.customization.yll.wuling.archive.constants;

/**
 * @author yaolilin
 * @desc 归档结果查询建模中归档状态字典
 * @date 2024/8/29
 **/
public class ArchiveStatus {
    private ArchiveStatus(){}
    public static final int DATA_ERROR = 0;
    public static final int CHECK_PASSED = 1;
    public static final int SUCCESS = 2;
    public static final int NOT_NEED_ARCHIVE = 3;
    public static final int CANCEL_ARCHIVE = 4;
    public static final int FAILED = 5;
    public static final int WAITING = 6;
}
