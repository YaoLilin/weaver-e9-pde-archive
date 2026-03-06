package com.customization.yll.wuling.archive.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.customization.yll.common.IntegrationLog;
import com.customization.yll.wuling.archive.util.HttpUtils;
import com.customization.yll.wuling.archive.util.PdeApiParamUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 姚礼林
 * @desc 查询归档结果
 * @date 2025/9/30
 **/
public class QueryArchiveStatusServiceImpl implements QueryArchiveStatusService {
    private final IntegrationLog log = new IntegrationLog(this.getClass());


    @Override
    public String queryStatus(String host, String apiUrl, QueryArchiveStatusParam param) {
        Map<String, Object> params = buildParams(param);
        log.info("请求参数：{}", JSON.toJSONString(params));
        JSONObject result = HttpUtils.post(host, apiUrl, JSON.toJSONString(params), JSONObject.class);
        if (result == null) {
            return "";
        }
        return result.toJSONString();
    }

    @NotNull
    private static Map<String, Object> buildParams(QueryArchiveStatusParam param) {
        Map<String, Object> header = PdeApiParamUtil.buildHeader(param.getRequestId(),
                param.getCommunicationCode(), "QueryArchiveStatus");
        List<Map<String, Object>> batchContents = buildBatchContents(param);

        Map<String, Object> body = PdeApiParamUtil.buildBoy(param.getRequestId() + "-" +
                System.currentTimeMillis(), batchContents);
        Map<String, Object> footer = PdeApiParamUtil.buildFoot(new JSONObject(header), new JSONObject(body),
                param.getRequestId());

        return PdeApiParamUtil.createParams(header, body, footer);
    }

    @NotNull
    private static List<Map<String, Object>> buildBatchContents(QueryArchiveStatusParam param) {
        List<Map<String, Object>> batchContents = new ArrayList<>();
        for (String sourceUniqueId : param.getSourceUniqueIds()) {
            Map<String, Object> batchContent = new HashMap<>(1);
            batchContent.put("sourceUniqueId", sourceUniqueId);
            batchContents.add(batchContent);
        }
        return batchContents;
    }
}
