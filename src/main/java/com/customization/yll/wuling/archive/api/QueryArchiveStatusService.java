package com.customization.yll.wuling.archive.api;

/**
 * @author 姚礼林
 * @desc 查询归档结果
 * @date 2025/9/30
 **/
public interface QueryArchiveStatusService {

    /**
     * 查询归档结果
     *
     * @param host   接口主机地址
     * @param param  查询参数
     * @param apiUrl 接口地址
     * @return 档案查询接口返回结果，为json字符串
     */
    String queryStatus(String host, String apiUrl, QueryArchiveStatusParam param);
}
