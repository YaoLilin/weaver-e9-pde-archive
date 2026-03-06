package com.customization.yll.wuling.archive.service;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author yaolilin
 * @desc 档案推送结果
 * @date 2024/10/29
 **/
@Data
@AllArgsConstructor
public class PushResult {
    private boolean success;
    private String message;
}
