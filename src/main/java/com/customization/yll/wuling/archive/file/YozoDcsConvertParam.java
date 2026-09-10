package com.customization.yll.wuling.archive.file;

import com.customization.yll.wuling.archive.constants.YozoDcsConvertType;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 永中 DCS 文件转换调用参数。
 *
 * @author 姚礼林
 * @date 2026/9/5
 */
public class YozoDcsConvertParam {
    /** 永中 DCS 服务根地址，不包含 {@code /composite/upload}。 */
    private final String serverAddress;
    /** 永中 DCS 的转换类型。 */
    private final YozoDcsConvertType convertType;
    /**
     * 请求头。key 为 HTTP Header 名称，value 为 Header 值；由调用方按客户 DCS 的认证规则传入。
     */
    private final Map<String, String> requestHeaders;
    /**
     * DCS 通用表单参数。key 为 DCS 参数名称，value 为参数值，例如 {@code convertTimeOut}、{@code password}。
     */
    private final Map<String, String> commonParams;

    public YozoDcsConvertParam(String serverAddress, YozoDcsConvertType convertType,
                               Map<String, String> requestHeaders, Map<String, String> commonParams) {
        this.serverAddress = serverAddress;
        this.convertType = convertType;
        this.requestHeaders = requestHeaders == null
                ? Collections.<String, String>emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<>(requestHeaders));
        this.commonParams = commonParams == null
                ? Collections.<String, String>emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<>(commonParams));
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public YozoDcsConvertType getConvertType() {
        return convertType;
    }

    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    public Map<String, String> getCommonParams() {
        return commonParams;
    }
}
