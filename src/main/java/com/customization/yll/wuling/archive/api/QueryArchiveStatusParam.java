package com.customization.yll.wuling.archive.api;

import lombok.Data;

import java.util.List;

/**
 * @author 姚礼林
 * @desc 查询归档结果参数
 * @date 2025/9/30
 **/
@Data
public class QueryArchiveStatusParam {
    private String requestId;
    private String communicationCode;
    private List<String> sourceUniqueIds;

    public QueryArchiveStatusParam(String requestId, String communicationCode, List<String> sourceUniqueIds) {
        this.requestId = requestId;
        this.communicationCode = communicationCode;
        this.sourceUniqueIds = sourceUniqueIds;
    }
}
