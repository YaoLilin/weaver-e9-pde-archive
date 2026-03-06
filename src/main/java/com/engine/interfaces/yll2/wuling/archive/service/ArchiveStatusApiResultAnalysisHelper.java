package com.engine.interfaces.yll2.wuling.archive.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.customization.yll.common.IntegrationLog;
import com.customization.yll.common.web.exception.ApiResultFailedException;
import com.customization.yll.wuling.archive.constants.ArchiveStatus;
import com.engine.interfaces.yll2.wuling.archive.bean.ArchiveResult;
import com.engine.interfaces.yll2.wuling.archive.util.ArchiveApiResultVerifyUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author 姚礼林
 * @desc 档案系统归档查询接口返回结果解析帮助类
 * @date 2025/10/9
 **/
public class ArchiveStatusApiResultAnalysisHelper {
    private final IntegrationLog log = new IntegrationLog(ArchiveStatusApiResultAnalysisHelper.class);

    /**
     * 解析档案系统归档查询接口返回结果
     *
     * @param apiResult 档案系统接口返回结果
     * @return 归档结果
     * @throws ApiResultFailedException 接口返回错误
     */
    public List<ArchiveResult> analysis(String apiResult) throws ApiResultFailedException {
        try {
            List<ArchiveResult> resultList = new ArrayList<>();
            JSONObject json = JSON.parseObject(apiResult);
            JSONObject body = json.getJSONObject("body");
            ArchiveApiResultVerifyUtil.VerifyResult verify = ArchiveApiResultVerifyUtil.verify(apiResult);
            if (!verify.isSuccess()) {
                throw new ApiResultFailedException("接口返回失败：" + verify.getMessage());
            }

            JSONArray batchContents = body.getJSONObject("data")
                    .getJSONObject("batchInfo").getJSONArray("batchContents");
            for (int i = 0; i < batchContents.size(); i++) {
                JSONObject item = batchContents.getJSONObject(i);
                String sourceUniqueId = item.getString("sourceUniqueId");
                JSONObject statusDetails = item.getJSONObject("statusDetails");
                if (statusDetails == null) {
                    continue;
                }
                ArchiveResult archiveStatus = getArchiveStatus(statusDetails, sourceUniqueId);
                resultList.add(archiveStatus);
            }
            return resultList;

        } catch (Exception e) {
            log.error("解析档案系统接口返回结果出错：" + e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private ArchiveResult getArchiveStatus(JSONObject param, String sourceId) {
        JSONObject dataErrorInfo = param.getJSONObject("dataErrorInfo");
        String messageKeyName = "message";
        String msg = "";
        if (dataErrorInfo != null) {
            msg = dataErrorInfo.getString(messageKeyName);
            if ("-002".equals(dataErrorInfo.getString("status"))) {
                return new ArchiveResult(ArchiveStatus.DATA_ERROR, msg, sourceId);
            }
        }
        JSONObject dataArchived = param.getJSONObject("dataArchived");
        if (dataArchived != null) {
            if ("002".equals(dataArchived.getString("status"))) {
                return new ArchiveResult(ArchiveStatus.SUCCESS, dataArchived.getString(messageKeyName), sourceId);
            } else if ("003".equals(dataArchived.getString("status"))) {
                return new ArchiveResult(ArchiveStatus.CANCEL_ARCHIVE, dataArchived.getString(messageKeyName), sourceId);
            }
        }
        JSONObject nonArchivable = param.getJSONObject("nonArchivable");
        if (nonArchivable != null && "003".equals(nonArchivable.getString("status"))) {
            return new ArchiveResult(ArchiveStatus.NOT_NEED_ARCHIVE,
                    nonArchivable.getString(messageKeyName), sourceId);
        }
        JSONObject integrityCheck = param.getJSONObject("integrityCheck");
        if (integrityCheck != null && "001".equals(integrityCheck.getString("status"))) {
            return new ArchiveResult((ArchiveStatus.CHECK_PASSED),
                    integrityCheck.getString(messageKeyName), sourceId);
        }
        return new ArchiveResult(ArchiveStatus.WAITING, msg, sourceId);
    }
}
