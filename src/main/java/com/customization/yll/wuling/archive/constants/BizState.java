package com.customization.yll.wuling.archive.constants;

import lombok.Getter;

/**
 * @author yaolilin
 * @desc 业务流程日志文件中的业务状态类型
 * @date 2024/9/12
 **/
public enum BizState {
    HISTORY("历史行为"),
    PLAN_TASK("计划任务");

    @Getter
    private final String name;

    BizState(String name) {
        this.name = name;
    }
}
