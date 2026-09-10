package com.customization.yll.wuling.archive.file;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.customization.yll.common.exception.DocConvertException;
import com.customization.yll.common.web.util.ApiCallManager;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import weaver.integration.logging.Logger;
import weaver.integration.logging.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 永中 DCS 文件转换接口调用管理器。
 *
 * @author 姚礼林
 * @date 2026/9/5
 */
public class YozoDcsConvertManager {
    private static final String CONVERT_PATH = "/composite/upload";
    private static final String CONVERT_TYPE_PARAM_NAME = "convertType";
    private static final String FILE_PARAM_NAME = "file";
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final ApiCallManager apiCallManager;

    public YozoDcsConvertManager(ApiCallManager apiCallManager) {
        this.apiCallManager = apiCallManager;
    }

    /**
     * 上传本地文件至私有部署的永中 DCS，并下载转换后的文件。
     *
     * @param sourceFile  待转换的源文件
     * @param savePath    转换后文件的本地保存路径
     * @param param       永中 DCS 服务地址、转换类型、认证请求头等调用参数
     */
    public void convert(File sourceFile, String savePath, YozoDcsConvertParam param) {
        if (param == null || StrUtil.isBlank(param.getServerAddress())) {
            throw new DocConvertException("永中 DCS 服务地址不能为空");
        }
        if (param.getConvertType() == null) {
            throw new DocConvertException("永中 DCS 转换类型不能为空");
        }
        String serverAddress = param.getServerAddress();
        if (!sourceFile.isFile()) {
            throw new DocConvertException("永中 DCS 转换源文件不存在，path：" + sourceFile.getAbsolutePath());
        }
        String convertUrl = appendPath(serverAddress, CONVERT_PATH);
        log.debug("调用永中 DCS 文件转换接口，convertUrl：" + convertUrl + "，sourcePath：" + sourceFile.getAbsolutePath());
        JSONObject response = upload(convertUrl, sourceFile, param);
        validateResponse(response);
        String viewUrl = getViewUrl(response);
        String downloadUrl = toAbsoluteUrl(serverAddress, viewUrl);
        log.debug("下载永中 DCS 转换后的文件，downloadUrl：" + downloadUrl + "，savePath：" + savePath);
        download(downloadUrl, savePath, param.getRequestHeaders());
    }

    private JSONObject upload(String convertUrl, File sourceFile, YozoDcsConvertParam param) {
        RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), sourceFile);
        List<MultipartBody.Part> parts = new ArrayList<>();
        parts.add(MultipartBody.Part.createFormData(CONVERT_TYPE_PARAM_NAME,
                String.valueOf(param.getConvertType().getCode())));
        parts.add(MultipartBody.Part.createFormData(FILE_PARAM_NAME, sourceFile.getName(), fileBody));
        addCommonParams(parts, param.getCommonParams());
        try (Response response = apiCallManager.uploadFile(convertUrl, parts, param.getRequestHeaders())) {
            if (!response.isSuccessful()) {
                throw new DocConvertException("调用永中 DCS 文件转换接口失败，httpCode：" + response.code());
            }
            ResponseBody body = response.body();
            if (body == null) {
                throw new DocConvertException("调用永中 DCS 文件转换接口失败，接口未返回响应内容");
            }
            String responseBody = body.string();
            log.debug("永中 DCS 文件转换接口返回：" + responseBody);
            return JSON.parseObject(responseBody);
        } catch (IOException e) {
            throw new DocConvertException("调用永中 DCS 文件转换接口发生异常", e);
        }
    }

    private void validateResponse(JSONObject response) {
        if (response == null) {
            throw new DocConvertException("永中 DCS 文件转换接口返回为空");
        }
        Integer errorCode = response.getInteger("errorcode");
        if (errorCode == null || errorCode != 0) {
            throw new DocConvertException("永中 DCS 文件转换失败，errorcode：" + errorCode
                    + "，message：" + response.getString("message"));
        }
    }

    private String getViewUrl(JSONObject response) {
        JSONObject data = response.getJSONObject("data");
        String viewUrl = data == null ? "" : data.getString("viewUrl");
        if (StrUtil.isBlank(viewUrl)) {
            throw new DocConvertException("永中 DCS 转换成功但未返回转换文件下载地址，response：" + response);
        }
        return viewUrl;
    }

    private void download(String downloadUrl, String savePath, Map<String, String> requestHeaders) {
        try (Response response = apiCallManager.get(downloadUrl, null, requestHeaders)) {
            if (!response.isSuccessful()) {
                throw new DocConvertException("下载永中 DCS 转换文件失败，httpCode：" + response.code());
            }
            ResponseBody body = response.body();
            if (body == null) {
                throw new DocConvertException("下载永中 DCS 转换文件失败，接口未返回文件内容");
            }
            try (InputStream inputStream = body.byteStream()) {
                Files.copy(inputStream, Paths.get(savePath), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new DocConvertException("下载永中 DCS 转换文件发生异常", e);
        }
    }

    private String appendPath(String serverAddress, String path) {
        return removeTrailingSlash(serverAddress) + path;
    }

    private String toAbsoluteUrl(String serverAddress, String viewUrl) {
        if (viewUrl.startsWith("http://") || viewUrl.startsWith("https://")) {
            return viewUrl;
        }
        URI serverUri = URI.create(removeTrailingSlash(serverAddress));
        String origin = serverUri.getScheme() + "://" + serverUri.getAuthority();
        return origin + (viewUrl.startsWith("/") ? viewUrl : "/" + viewUrl);
    }

    private String removeTrailingSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private void addCommonParams(List<MultipartBody.Part> parts, Map<String, String> commonParams) {
        for (Map.Entry<String, String> entry : commonParams.entrySet()) {
            String paramName = entry.getKey();
            if (CONVERT_TYPE_PARAM_NAME.equals(paramName) || FILE_PARAM_NAME.equals(paramName)) {
                log.warn("忽略永中 DCS 通用参数中的保留字段：" + paramName);
                continue;
            }
            if (StrUtil.isNotBlank(paramName) && entry.getValue() != null) {
                parts.add(MultipartBody.Part.createFormData(paramName, entry.getValue()));
            }
        }
    }

}
