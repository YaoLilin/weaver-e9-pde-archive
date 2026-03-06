package com.engine.interfaces.yll2.wuling.archive.bean;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author yaolilin
 * @desc 归档状态
 * @date 2024/8/29
 **/
@Data
@AllArgsConstructor
public class ArchiveResult {
    private int status;
    private String message;
    private String sourceId;
}
