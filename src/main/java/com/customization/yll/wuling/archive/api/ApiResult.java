package com.customization.yll.wuling.archive.api;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author yaolilin
 * @desc 档案推送接口结果
 * @date 2024/9/27
 **/
@Data
@AllArgsConstructor
public class ApiResult {
    private boolean success;
    private String params;
    private String result;
    /**
     * 档案数据包id
     */
    private String sourceId;
}
