package com.customization.yll.wuling.archive.file;

import cn.hutool.core.util.StrUtil;
import com.customization.yll.common.IntegrationLog;
import com.customization.yll.common.web.util.ApiCallManager;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 姚礼林
 * @desc 下载契约锁签署文档
 * @date 2025/8/8
 **/
public class QysSignatureDocDownloadManger {
    private final ApiCallManager apiCallManager = new ApiCallManager();
    private final IntegrationLog log = new IntegrationLog(this.getClass());
    private final String serverAddress;
    private final String token;
    private final String secret;

    public QysSignatureDocDownloadManger(String serverAddress, String token, String secret) {
        this.serverAddress = serverAddress;
        this.token = token;
        this.secret = secret;
    }

    /**
     * 下载签署文档
     *
     * @param documentId 签署文档ID
     * @return 下载的文件
     */
    public File downloadDoc(long documentId,String saveDir) {
        return downloadDoc(documentId, saveDir,null,null);
    }

    /**
     * 下载签署文档（带操作人信息）
     *
     * @param documentId   签署文档ID
     * @param operatorName 操作人姓名
     * @return 下载的文件
     */
    public @Nullable File downloadDoc(long documentId, String saveDir,@Nullable String documentFormat,
                                      @Nullable String operatorName) {
        // 验证配置
        if (StrUtil.isBlank(serverAddress) || StrUtil.isBlank(token) || StrUtil.isBlank(secret)) {
            throw new IllegalArgumentException("契约锁API配置无效，请检查qysServerAddress、qysToken和qysSecret配置");
        }
        log.info("开始下载签署文档,文档id：" + documentId);
        Map<String, String> params = getParams(documentId, documentFormat, operatorName);
        Map<String, String> header;
        try {
            header = getHeader();
        } catch (NoSuchAlgorithmException e) {
            log.error("生成签名失败, 无此加密算法", e);
            return null;
        }

        String fileName;
        if (StrUtil.isNotBlank(documentFormat)) {
            fileName = "document_" + documentId + "." + documentFormat;
        }else {
            fileName = "document_" + documentId + ".pdf";
        }
        String fileSavePath = Paths.get(saveDir, fileName).toString();

        // 执行请求
        try (Response response = apiCallManager.get(serverAddress+"/document/download", params, header)) {
            return getFile(fileSavePath, response);
        } catch (IOException e) {
            log.error("接口调用发生异常", e);
            return null;
        }
    }

    @NotNull
    private static Map<String, String> getParams(long documentId, @Nullable String documentFormat,
                                                 @Nullable String operatorName) {
        Map<String, String> params = new HashMap<>(3);
        params.put("documentId", String.valueOf(documentId));
        if (StrUtil.isNotBlank(operatorName)) {
            params.put("name", operatorName);
        }
        if (StrUtil.isNotBlank(documentFormat)) {
            params.put("documentFormat", "pdf".equals(documentFormat) ? "PDF_BLACK_SEAL" : "OFD_BLACK_SEAL");
        }
        return params;
    }

    @Nullable
    private File getFile(String savePath, Response response) throws IOException {
        if (!response.isSuccessful()) {
            log.error("下载失败，HTTP状态码: {}", response.code());
            if (response.body() != null) {
                log.error("返回信息：{}", response.body().string());
            }
            return null;
        }

        // 获取响应体
        ResponseBody responseBody = response.body();
        if (responseBody == null) {
            log.error("响应体为空");
            return null;
        }

        log.info("接口返回成功，正在保存文件，文件路径：{}", savePath);
        // 保存文件
        File file = new File(savePath);
        Files.copy(responseBody.byteStream(), Paths.get(savePath));
        log.info("保存文件成功");
        return file;
    }

    @NotNull
    private Map<String, String> getHeader() throws NoSuchAlgorithmException {
        // 生成时间戳和签名
        long timestamp = System.currentTimeMillis();
        String signature = generateSignature(timestamp);

        Map<String, String> header = new HashMap<>(3);
        header.put("x-qys-accesstoken", token);
        header.put("x-qys-timestamp", String.valueOf(timestamp));
        header.put("x-qys-signature", signature);
        header.put("Accept", "text/plain,application/json");
        return header;
    }

    /**
     * 生成签名
     *
     * @param timestamp 时间戳
     * @return 签名字符串
     */
    private String generateSignature(long timestamp) throws NoSuchAlgorithmException {
        String source = token + secret + timestamp;
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(source.getBytes(StandardCharsets.UTF_8));

        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

}
