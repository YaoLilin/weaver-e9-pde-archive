package com.customization.yll.wuling.archive.util;

import org.apache.commons.lang.StringUtils;
import weaver.conn.RecordSet;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 公共归档数据获取
 * @author yaolilin
 */
public class CommonArchiveDataUtil {

    public static String getCompanyName() {
        RecordSet recordSet = new RecordSet();
        recordSet.executeQuery("SELECT COMPANYNAME from hrmcompany");
        recordSet.next();
        String companyName = recordSet.getString("COMPANYNAME");
        if (StringUtils.isEmpty(companyName)) {
            return "广西汽车集团有限公司";
        }
        return companyName;
    }

    public static String getCurrentDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return LocalDate.now().format(formatter);
    }

    public static String getYear() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy");
        return LocalDate.now().format(formatter);
    }
}
