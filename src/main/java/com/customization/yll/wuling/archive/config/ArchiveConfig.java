package com.customization.yll.wuling.archive.config;

import cn.hutool.core.util.StrUtil;
import com.customization.yll.common.util.PropertiesUtil;
import weaver.integration.util.ConstantsMethodUtil;

/**
 * 档案系统接口配置
 * @author yaolilin
 */
public class ArchiveConfig {
    public static final String CONFIG_FILE_NAME = "FW20240726-archiveSystem";

    public static String getCode() {
        return PropertiesUtil.getPropValue(CONFIG_FILE_NAME, "code", true,
                true,PropertiesUtil.DEFAULT_EXPIRE_THREE_MINUTES);
    }

    public static String getServerAddress() {
        return PropertiesUtil.getPropValue(CONFIG_FILE_NAME, "serverAddress", true,
                true,PropertiesUtil.DEFAULT_EXPIRE_THREE_MINUTES);
    }

    public static String getFileUploadUrl() {
        return PropertiesUtil.getPropValue(CONFIG_FILE_NAME, "fileUploadUrl", true,
                true,PropertiesUtil.DEFAULT_EXPIRE_THREE_MINUTES);
    }

    /**
     * @return 获取档案系统归档接口地址
     */
    public static String getArchiveUrl() {
        return PropertiesUtil.getPropValue(CONFIG_FILE_NAME, "archiveUrl", true,
                true,PropertiesUtil.DEFAULT_EXPIRE_THREE_MINUTES);
    }

    /**
     * @return 获取档案系统查询归档状态接口地址，不带服务器地址
     */
    public static String getQueryStatusUrl() {
        return PropertiesUtil.getPropValue(CONFIG_FILE_NAME, "queryStatusUrl", true,
                true, PropertiesUtil.DEFAULT_EXPIRE_THREE_MINUTES);
    }

    /**
     * 是否启用查询归档范围来判断是否需要归档,如果为是，则调用接口查询归档范围，根据题名关键字进行查询是否在归档范围内，
     * 如果不在归档范围内则不需要推送到档案系统进行归档，为否时，不进行归档范围判断，直接推送到档案系统
     *
     * @return 是否启用查询归档范围
     */
    public static boolean enableArchivingScope() {
        return "1".equals(PropertiesUtil.getPropValue(CONFIG_FILE_NAME, "enableArchivingScope", true,
                true));
    }

    public static String getArchivingScopeUrl() {
        return PropertiesUtil.getPropValue(CONFIG_FILE_NAME, "archivingScopeUrl", true,
                true,PropertiesUtil.DEFAULT_EXPIRE_THREE_MINUTES);
    }

    public static String getTableId() {
        return PropertiesUtil.getPropValue(CONFIG_FILE_NAME, "tableId", true,
                true,PropertiesUtil.DEFAULT_EXPIRE_THREE_MINUTES);
    }


    public static String getSsoUrl() {
        return PropertiesUtil.getPropValue(CONFIG_FILE_NAME, "ssoUrl", true,
                true,PropertiesUtil.DEFAULT_EXPIRE_THREE_MINUTES);
    }

    public static String getPassword() {
        return PropertiesUtil.getPropValue(CONFIG_FILE_NAME, "password", true,
                true,PropertiesUtil.DEFAULT_EXPIRE_THREE_MINUTES);
    }

    public static String getUserName() {
        return PropertiesUtil.getPropValue(CONFIG_FILE_NAME, "userName", true,
                true,PropertiesUtil.DEFAULT_EXPIRE_THREE_MINUTES);
    }

    public static String getDeleteTempFiles() {
        return PropertiesUtil.getPropValue(CONFIG_FILE_NAME, "deleteTempFiles", false,
                true,PropertiesUtil.DEFAULT_EXPIRE_THREE_MINUTES);
    }

    public static String getFileConvertMethod() {
        return PropertiesUtil.getPropValue(CONFIG_FILE_NAME, "fileConvertMethod", true,
                true,PropertiesUtil.DEFAULT_EXPIRE_THREE_MINUTES);
    }

    public static String getWpsServerHost() {
        return PropertiesUtil.getPropValue(CONFIG_FILE_NAME, "wpsServerHost", false,
                true,PropertiesUtil.DEFAULT_EXPIRE_THREE_MINUTES);
    }

    public static String getWpsSecret() {
        return PropertiesUtil.getPropValue(CONFIG_FILE_NAME, "wpsSecret", false,
                true,PropertiesUtil.DEFAULT_EXPIRE_THREE_MINUTES);
    }

    public static String getWpsAccessKey() {
        return PropertiesUtil.getPropValue(CONFIG_FILE_NAME, "wpsAccessKey", false,
                true,PropertiesUtil.DEFAULT_EXPIRE_THREE_MINUTES);
    }

    public static String getOaAddress() {
        String oaAddress = PropertiesUtil.getPropValue(CONFIG_FILE_NAME, "oaAddress", false,
                true, PropertiesUtil.DEFAULT_EXPIRE_THREE_MINUTES);
        if (StrUtil.isEmpty(oaAddress)) {
            return ConstantsMethodUtil.getOaAddress();
        }
        return oaAddress;
    }

    public static boolean getEnableQysSignatureVerify() {
        String enableQysSignatureVerify = PropertiesUtil.getPropValue(CONFIG_FILE_NAME,
                "enableQysSignatureVerify", false,
                true, PropertiesUtil.DEFAULT_EXPIRE_THREE_MINUTES);
        return "1".equals(enableQysSignatureVerify);
    }

    public static String getQysServerAddress() {
        return PropertiesUtil.getPropValue(CONFIG_FILE_NAME, "qysServerAddress", false,
                true, PropertiesUtil.DEFAULT_EXPIRE_THREE_MINUTES);
    }

    public static String getQysToken() {
        return PropertiesUtil.getPropValue(CONFIG_FILE_NAME, "qysToken", false,
                true, PropertiesUtil.DEFAULT_EXPIRE_THREE_MINUTES);
    }

    public static String getQysSecret() {
        return PropertiesUtil.getPropValue(CONFIG_FILE_NAME, "qysSecret", false,
                true, PropertiesUtil.DEFAULT_EXPIRE_THREE_MINUTES);
    }

    /**
     * 来源系统代码，填当前系统名称，如：OA系统
     *
     * @return 来源系统代码
     */
    public static String getSourceSystemCode() {
        return PropertiesUtil.getPropValueWithChineseHandle(CONFIG_FILE_NAME, "sourceSystemCode", true,
                true, PropertiesUtil.DEFAULT_EXPIRE_THREE_MINUTES);
    }

    /**
     * 公司名称，填当前公司名称，如：广西汽车集团有限公司
     *
     * @return 公司名称
     */
    public static String getCompanyName() {
        return PropertiesUtil.getPropValueWithChineseHandle(CONFIG_FILE_NAME, "companyName", true,
                true, PropertiesUtil.DEFAULT_EXPIRE_THREE_MINUTES);
    }
}
