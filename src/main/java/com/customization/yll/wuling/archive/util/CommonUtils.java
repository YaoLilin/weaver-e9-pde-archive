package com.customization.yll.wuling.archive.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.SM3;
import org.apache.commons.codec.binary.Hex;

import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;

/**
 * @author Administrator
 */
public class CommonUtils implements Serializable {
    /**
     * 摘要算法：
     * 每个线程持有自己的SM3，解决SM3线程安全问题
     */
    private static final ThreadLocal<SM3> SM3_THREAD_LOCAL = ThreadLocal.withInitial(SM3::new);
    private static final long serialVersionUID = 1L;

    /**
     * Base64解码
     *
     * @param data 字符串
     * @return String
     * @throws UnsupportedEncodingException
     */
    public static String base64Decoder(String data) throws UnsupportedEncodingException {
        return new String(Base64.getDecoder().decode(data), "UTF-8");
    }

    /**
     * Base64编码
     *
     * @param data 字符串
     * @return String
     * @throws UnsupportedEncodingException
     */
    public static String base64Encoder(String data) throws UnsupportedEncodingException {
        return Base64.getEncoder().encodeToString(data.getBytes("UTF-8"));
    }

    /**
     * Base64编码-按照文件
     *
     * @param filePath 文件路径
     * @return String
     */
    public static String base64EncoderFile(String filePath) {
        return Base64.getEncoder().encodeToString(FileUtil.readBytes(filePath));
    }

    /**
     * Base64解码-按照文件
     *
     * @param filePath 文件路径
     * @return String
     * @throws UnsupportedEncodingException
     */
    public static String base64DecoderFile(String filePath) throws UnsupportedEncodingException {
        return new String(Base64.getDecoder().decode(FileUtil.readUtf8String(filePath)), "UTF-8");
    }

    public static String getTime(String format) {
        LocalDateTime time = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return formatter.format(time);
    }

    public static String getTime() {
        return getTime("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    }

    public static String getUUID() {
        return String.valueOf(UUID.randomUUID()).replaceAll("-", "").toUpperCase();
    }

    public static String getFileName(String asipLocalPath) {
        String suffix = asipLocalPath.substring(asipLocalPath.lastIndexOf(".") + 1);
        return StrUtil.removeSuffix(FileUtil.getName(asipLocalPath), "." + suffix);
    }

    public static String getFileDigest(String asipLocalPath, String algorithmId) {
        String sysPackageDigest = "";
        InputStream inputStream = null;
        try {
            inputStream = Files.newInputStream(Paths.get(asipLocalPath));
            sysPackageDigest = SM3_THREAD_LOCAL.get().digestHex(inputStream);
        } catch (Exception e) {
            sysPackageDigest = "ERROR:" + e.getMessage();
            e.printStackTrace();
        } finally {
            IoUtil.close(inputStream);
        }
        return sysPackageDigest;
    }

    public static String getDataDigest(String data, String algorithmId) {
        String sysPackageDigest = "";
        try {
            if ("SM3".equals(algorithmId)) {
                sysPackageDigest = SM3_THREAD_LOCAL.get().digestHex(data);
            } else {
                MessageDigest md = MessageDigest.getInstance(algorithmId);
                md.update(data.getBytes("utf-8"));
                sysPackageDigest = String.valueOf(Hex.encodeHex(md.digest()));
            }
        } catch (Exception e) {
            sysPackageDigest = "ERROR";
            e.printStackTrace();
        }
        return sysPackageDigest;
    }

}
