package com.customization.yll.wuling.archive.api;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.customization.yll.common.exception.PropNotConfigureException;
import com.customization.yll.common.util.WorkflowUtil;
import com.customization.yll.wuling.archive.config.ArchiveConfig;
import com.customization.yll.wuling.archive.exception.UploadPackageException;
import com.customization.yll.wuling.archive.util.CommonUtils;
import com.customization.yll.wuling.archive.util.HttpUtils;
import com.customization.yll.wuling.archive.util.PdeApiParamUtil;
import com.pde.ams.file.base.domain.Result;
import com.pde.ams.file.base.domain.ServerInfoDTO;
import com.pde.ams.file.client.service.UploadTaskHessianService;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import weaver.conn.RecordSet;
import weaver.integration.logging.Logger;
import weaver.integration.logging.LoggerFactory;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 推送档案信息到档案系统
 * @author yaolilin
 */
public class ArchiveDataPushManager {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    /**
     * 数据接收平台：归档地址
     */
    protected final String archiveUrl;
    /**
     * 数据接收平台：通信码
     */
    protected final String communicationCode;
    /**
     * 数据接收平台：文件上传地址
     */
    protected final String uploadServerUrl;
    /**
     * 数据接收平台：文件上传账号
     */
    protected final String username;
    /**
     * 数据接收平台：文件上传密码
     */
    protected final String password;
    protected final String serverAddress;

    public ArchiveDataPushManager() {
        this.serverAddress = ArchiveConfig.getServerAddress();
        this.communicationCode = ArchiveConfig.getCode();
        this.uploadServerUrl = ArchiveConfig.getFileUploadUrl();
        this.archiveUrl = ArchiveConfig.getArchiveUrl();
        this.password = ArchiveConfig.getPassword();
        this.username = ArchiveConfig.getUserName();
    }

    /**
     * 将sip包以json方式推送到档案系统
     *
     * @param requestId 流程请求id
     * @param packagePath sip档案包路径
     * @return 推送结果
     * @exception FileNotFoundException 档案信息包不存在
     * @exception UploadPackageException 档案信息包上传失败
     */
    public ApiResult push(int requestId, String packagePath) throws FileNotFoundException, UploadPackageException {
        if (StringUtils.isEmpty(packagePath)) {
            throw new FileNotFoundException("sip包不存在");
        }
        validConfiguration();
        String sourceId = getSourceId(requestId, packagePath);
        // 文件上传,上传成功返回相对路径
        String relativePath = upload(packagePath);
        Map<String, Object> requestBody = buildParams(requestId, packagePath, relativePath, sourceId);
        String jsonStr = JSON.toJSONString(requestBody);
        logger.info("请求参数："+jsonStr);
        Map responseMap = HttpUtils.post(serverAddress, archiveUrl, jsonStr, Map.class);
        String resultJson = new JSONObject(responseMap).toJSONString();
        logger.info("请求结果："+ resultJson);
        return new ApiResult(validResult(responseMap), jsonStr, resultJson, sourceId);
    }

    protected Map<String, Object> buildParams(int requestId, String packagePath, String relativePath, String sourceId) {
        String uuid = CommonUtils.getUUID();
        Map<String, Object> header = buildHeader(uuid);
        Map<String, Object> body = buildBoy(requestId, packagePath, uuid, relativePath, sourceId);
        Map<String, Object> footer = PdeApiParamUtil.buildFoot(new JSONObject(header), new JSONObject(body),
                String.valueOf(requestId));
        return PdeApiParamUtil.createParams(header, body, footer);
    }

    /**
     * 获取数据来源ID
     *
     * @param requestId   流程请求id
     * @param packagePath sip包路径
     * @return 数据来源ID
     */
    protected String getSourceId(int requestId, String packagePath) {
        return "OA" + requestId;
    }

    private @NotNull Map<String, Object> buildBoy(int requestId, String packagePath, String batchId,
                                                  String relativePath, String sourceId) {
        List<Map<String, Object>> batchContents = new ArrayList<>();
        Map<String, Object> batchContent = new HashMap<>(30);
        // 数据来源ID
        batchContent.put("sourceUniqueId", sourceId);
        // 单个ASIP文件的描述性信息
        batchContent.put("destination", WorkflowUtil.getRequestName(requestId, new RecordSet()));
        // ASIP包路径
        batchContent.put("sipPath", relativePath);
        // 数字摘要值
        String sysPackageDigest = CommonUtils.getFileDigest(packagePath, "SM3");
        batchContent.put("sysPackageDigest", sysPackageDigest);
        // 移交单位
        batchContent.put("sourceUnit", ArchiveConfig.getCompanyName());
        batchContents.add(batchContent);

        return PdeApiParamUtil.buildBoy(batchId, batchContents);
    }

    private @NotNull Map<String, Object> buildHeader(String requestId) {
        return PdeApiParamUtil.buildHeader(requestId, communicationCode, "Archive");
    }

    private void validConfiguration() {
        if (StringUtils.isEmpty(serverAddress)) {
            throw new PropNotConfigureException("服务器地址未配置");
        }
        if (StringUtils.isEmpty(communicationCode)) {
            throw new PropNotConfigureException("通信码未配置");
        }
        if (StringUtils.isEmpty(uploadServerUrl)) {
            throw new PropNotConfigureException("文件上传地址未配置");
        }
        if (StringUtils.isEmpty(archiveUrl)) {
            throw new PropNotConfigureException("数据接收平台归档地址未配置");
        }
        if (StringUtils.isEmpty(username)) {
            throw new PropNotConfigureException("文件上传账号未配置");
        }
        if (StringUtils.isEmpty(password)) {
            throw new PropNotConfigureException("文件上传密码未配置");
        }
    }

    /**
     * 将档案信息包上传到接口平台
     *
     * @param filePath 档案信息包在接口平台中的路径
     * @throws UploadPackageException 文件上传失败
     */
    protected String upload(String filePath) throws UploadPackageException {
        try {
            ServerInfoDTO serverInfoDTO = ServerInfoDTO.build(uploadServerUrl, username, password, 1024 * 1024 * 10);
            UploadTaskHessianService uploadTaskService = new UploadTaskHessianService(serverInfoDTO);
            Result result = uploadTaskService.uploadFile(ArchiveConfig.getSourceSystemCode(), filePath);
            logger.info("文件上传结果：" + result);
            if (!"SUCCESS".equals(result.getStatus())) {
                throw new UploadPackageException("文件上传失败，msg:" + result.getMessage());
            }
            return result.getData().toString();
        } catch (Exception e) {
            throw new UploadPackageException("文件上传失败", e);
        }
    }

    protected boolean validResult(Map responseMap) {
        Map bodyMap = MapUtil.get(responseMap, "body", Map.class);
        Map responseStatusMap = MapUtil.get(bodyMap, "responseStatus", Map.class);
        if (!"000".equals(MapUtil.getStr(responseStatusMap, "statusCode"))) {
            String failedMsg = MapUtil.getStr(responseStatusMap, "statusMessage");
            logger.error("调用档案推送接口返回失败，msg：" + failedMsg);
            return false;
        }
        return true;
    }

}
