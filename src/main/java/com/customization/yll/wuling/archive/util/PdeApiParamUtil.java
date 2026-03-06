package com.customization.yll.wuling.archive.util;

import com.alibaba.fastjson.JSONObject;
import com.customization.yll.wuling.archive.config.ArchiveConfig;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 姚礼林
 * @desc 档案系统接口工具类
 * @date 2025/9/30
 **/
@UtilityClass
public class PdeApiParamUtil {

    /**
     * 构建档案系统接口请求头
     *
     * @param requestId         请求id
     * @param communicationCode 档案系统通信码
     * @param actionFlag        操作标识： Archive：提交归档申请，QueryArchiveStatus：查询归档结果，FeedbackArchiveStatus：推送归档结果
     * @return 请求头
     */
    public static @NotNull Map<String, Object> buildHeader(String requestId, String communicationCode, String actionFlag) {
        Map<String, Object> header = new HashMap<>(6);
        header.put("requestId", requestId);
        header.put("timestamp", CommonUtils.getTime());
        header.put("sourceSystemCode", ArchiveConfig.getSourceSystemCode());
        header.put("communicationCode", communicationCode);
        header.put("actionFlag", actionFlag);
        header.put("version", "1.0.0.0");
        return header;
    }

    /**
     * 构建档案系统接口body
     *
     * @param batchId       批次ID，可以同requestId
     * @param batchContents 批次内容
     * @return body
     */
    public static @NotNull Map<String, Object> buildBoy(String batchId, List<Map<String, Object>> batchContents) {
        Map<String, Object> body = new HashMap<>(10);
        Map<String, Object> data = new HashMap<>(10);
        Map<String, Object> batchInfo = new HashMap<>(10);
        // 批次ID，可以同requestId
        batchInfo.put("batchId", batchId);
        batchInfo.put("batchContents", batchContents);
        data.put("batchInfo", batchInfo);
        body.put("data", data);
        return body;
    }

    /**
     * 构建档案系统接口footer
     *
     * @param header    接口请求参数 header 部分
     * @param body      接口请求参数 body 部分
     * @param requestId 请求id
     * @return footer
     */
    public static Map<String, Object> buildFoot(JSONObject header, JSONObject body, String requestId) {
        Map<String, Object> footer = new HashMap<>(4);
        String signature = CommonUtils.getDataDigest(header + body.toJSONString(), "SM3");
        footer.put("signature", signature);
        footer.put("logId", requestId + "-" + System.currentTimeMillis());
        footer.put("totalTimeTaken", "");
        footer.put("serverVersion", "");
        return footer;
    }

    /**
     * 构建档案系统接口参数
     *
     * @param header 请求头
     * @param body   body
     * @param footer footer
     * @return 参数
     */
    public static Map<String, Object> createParams(Map<String, Object> header,
                                                   Map<String, Object> body, Map<String, Object> footer) {
        Map<String, Object> params = new HashMap<>(4);
        params.put("header", header);
        params.put("body", body);
        params.put("footer", footer);
        return params;
    }
}
