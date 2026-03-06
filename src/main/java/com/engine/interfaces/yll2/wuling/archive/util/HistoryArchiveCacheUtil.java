package com.engine.interfaces.yll2.wuling.archive.util;

import com.customization.yll.common.util.CacheUtil;

/**
 * @author yaolilin
 * @desc 历史流程推送缓存工具
 * @date 2025/1/13
 **/
public class HistoryArchiveCacheUtil {
    private static final int CACHE_EXPIRE_FIVE_HOURS = 60 * 60 * 5;
    private HistoryArchiveCacheUtil() {
    }

    public static final String HISTORY_ARCHIVE_PUSHING_FLAG_CACHE_KEY = "historyArchivePushing";
    public static final String HISTORY_ARCHIVE_PUSH_DONE_COUNT_CACHE_KEY = "historyArchivePushDoneCount";
    public static final String HISTORY_ARCHIVE_PUSH_FAILED_COUNT_CACHE_KEY = "historyArchivePushFailedCount";
    public static final String HISTORY_ARCHIVE_PUSH_TOTAL_COUNT_CACHE_KEY = "historyArchivePushTotalCount";

    public static void initCache(int pushCount) {
        CacheUtil.putCache(HISTORY_ARCHIVE_PUSHING_FLAG_CACHE_KEY, 1, CACHE_EXPIRE_FIVE_HOURS);
        CacheUtil.putCache(HISTORY_ARCHIVE_PUSH_TOTAL_COUNT_CACHE_KEY, pushCount, CACHE_EXPIRE_FIVE_HOURS);
        CacheUtil.putCache(HISTORY_ARCHIVE_PUSH_DONE_COUNT_CACHE_KEY, 0, CACHE_EXPIRE_FIVE_HOURS);
        CacheUtil.putCache(HISTORY_ARCHIVE_PUSH_FAILED_COUNT_CACHE_KEY, 0, CACHE_EXPIRE_FIVE_HOURS);
    }

    public static void cleanCache() {
        CacheUtil.deleteCache(HISTORY_ARCHIVE_PUSHING_FLAG_CACHE_KEY);
        CacheUtil.deleteCache(HISTORY_ARCHIVE_PUSH_DONE_COUNT_CACHE_KEY);
        CacheUtil.deleteCache(HISTORY_ARCHIVE_PUSH_FAILED_COUNT_CACHE_KEY);
        CacheUtil.deleteCache(HISTORY_ARCHIVE_PUSH_TOTAL_COUNT_CACHE_KEY);
    }
}
