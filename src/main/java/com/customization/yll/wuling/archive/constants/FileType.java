package com.customization.yll.wuling.archive.constants;

import lombok.Getter;

/**
 *  文件类型
 * @author yaolilin
 */
@Getter
public enum FileType {
    MAIN_BODY("正文", 1),
    ATTACHMENT("附件", 2),
    APPROVAL_INFORMATION("审批信息",3),
    ;

    FileType(String name, int order) {
        this.name = name;
        this.order = order;
    }

    private final String name;
    private final int order;
}
