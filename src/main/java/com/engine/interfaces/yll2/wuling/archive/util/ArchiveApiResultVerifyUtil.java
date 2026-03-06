package com.engine.interfaces.yll2.wuling.archive.util;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.UtilityClass;

/**
 * @author 姚礼林
 * @desc 档案系统接口返回结果验证工具类
 * @date 2025/10/15
 **/
@UtilityClass
public class ArchiveApiResultVerifyUtil {
    private static final String ERROR_CODE = "-001";

    /**
     * 验证接口返回结果
     *
     * @param result 接口返回结果
     * @return 验证结果，包含接口是否返回成功，如果接口失败则会获取错误信息
     */
    public static VerifyResult verify(String result) {
        if (StrUtil.isBlank(result)) {
            return new VerifyResult(false, "接口结果为空");
        }
        JSONObject json = JSON.parseObject(result);
        JSONObject body = json.getJSONObject("body");
        if (body == null) {
            return new VerifyResult(true, "");
        }

        JSONObject responseStatus = body.getJSONObject("responseStatus");
        if (responseStatus != null && ERROR_CODE.equals(responseStatus.getString("statusCode"))) {
            return new VerifyResult(false, responseStatus.getString("statusMessage"));
        }
        return new VerifyResult(true, "");
    }

    @Data
    @AllArgsConstructor
    public static class VerifyResult {
        private boolean success;
        private String message;
    }
}
