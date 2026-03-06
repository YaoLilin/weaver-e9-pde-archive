package com.customization.yll.wuling.archive.service;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.customization.yll.common.util.CacheUtil;
import com.customization.yll.common.web.exception.ApiResultFailedException;
import com.customization.yll.common.web.util.ApiCallManager;
import com.customization.yll.wuling.archive.config.ArchiveConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import weaver.integration.logging.Logger;
import weaver.integration.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 姚礼林
 * @desc 归档范围校验，通过调用档案系统归档范围查询接口，如果流程不属于归档范围则不需要推送到档案系统
 * @date 2025/6/5
 **/
public class ArchiveScopeValidator {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private static final String SCOPE_DATA_CACHE_KEY = "archive:scope_data";
    private static final int CACHE_EXPIRE_TIME = 60 * 4;

    public ValidateResult validate(String subject) {
        List<ArchiveScopeData> scopeData = getCacheData();
        if (scopeData == null) {
            scopeData = callApiGetScopeData();
        }
        for (ArchiveScopeData item : scopeData) {
            if (subject.contains(item.getKeyword())) {
                log.info("符合归档范围，关键字：" + item.getKeyword());
                return new ValidateResult(true, item.getCategoryNumber(), item.getSaveYears());
            }
        }
        return new ValidateResult(false, null, null);
    }

    @Nullable
    private List<ArchiveScopeData> getCacheData() {
        List<ArchiveScopeData> scopeData = null;
        Object cacheValue = CacheUtil.getCache(SCOPE_DATA_CACHE_KEY);
        log.info("是否有缓存：" + (cacheValue != null));
        if (cacheValue != null) {
            scopeData = jsonArrayToList(JSON.parseArray((String) cacheValue));
        }
        return scopeData;
    }

    private List<ArchiveScopeData> callApiGetScopeData() {
        String result = callApi();
        log.info("接口返回数据：" + result);
        verifyResult(result);
        JSONObject resultJson = JSON.parseObject(result);
        JSONArray data = resultJson.getJSONArray("result");
        List<ArchiveScopeData> dataList = jsonArrayToList(data);
        CacheUtil.putCache(SCOPE_DATA_CACHE_KEY, JSON.toJSONString(data), CACHE_EXPIRE_TIME);
        return dataList;
    }

    private String callApi() {
        String apiUrl = ArchiveConfig.getArchivingScopeUrl();
        String tableId = ArchiveConfig.getTableId();
        JSONObject body = new JSONObject();
        body.put("tableId", Integer.parseInt(tableId));
        ApiCallManager apiCallManager = new ApiCallManager();
        return apiCallManager.postResult(apiUrl, body.toJSONString());
    }

    @NotNull
    private static List<ArchiveScopeData> jsonArrayToList(JSONArray array) {
        List<ArchiveScopeData> dataList = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            JSONObject object = array.getJSONObject(i);
            ArchiveScopeData archiveScopeData = new ArchiveScopeData();
            archiveScopeData.setKeyword(object.getString("keyword"));
            archiveScopeData.setCategoryNumber(object.getString("flh"));
            archiveScopeData.setSaveYears(object.getString("bgqx"));
            dataList.add(archiveScopeData);
        }
        return dataList;
    }

    private static void verifyResult(String result) {
        if (StrUtil.isEmpty(result)) {
            throw new ApiResultFailedException("接口请求失败，返回结果为空");
        }
        JSONObject resultJson = JSON.parseObject(result);
        if (!resultJson.getBooleanValue("state")) {
            throw new ApiResultFailedException("接口请求失败，" + resultJson.getString("message"));
        }
    }

}
