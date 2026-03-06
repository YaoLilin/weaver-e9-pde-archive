package com.pde.pdes.eep;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import com.customization.yll.common.util.ZipUtil;
import com.pde.pdes.eep.domain.BasicProperties;
import com.pde.pdes.eep.exception.EepPackException;
import com.pde.pdes.eep.util.EepPackUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author 姚礼林
 * @desc 量子档案打包工具，可生成量子档案 asip 包，此类是解决压缩报错问题，重写了压缩方法，解决压缩含有中文文件时发生报错的问题。
 * @date 2025/10/23
 **/
public class OverrideZipRecordEepPackager extends RecordEepPackager {

    public OverrideZipRecordEepPackager(BasicProperties properties) {
        super(properties, null);
    }

    /**
     * 重写压缩方法，解决压缩含有中文文件时发生报错的问题。在原来的压缩方法中，会使用 HuTool 的 ZipUtil.zip 方法进行压缩，
     * 但是在某些环境下，可能由于编码的原因， HuTool 的 ZipUtil.zip 方法在压缩含有中文文件时会报错，对编码处理的不够好。
     *
     * @param fileName 压缩包文件名
     * @return 压缩包文件
     * @throws EepPackException 压缩发生的异常
     */
    @Override
    public File doZip(String fileName) throws EepPackException {
        if (fileName != null && !fileName.isEmpty()) {
            File dir = this.doEnd();
            String suffix = FileNameUtil.getSuffix(fileName);
            if (suffix.isEmpty()) {
                fileName = fileName + ".zip";
            }

            fileName = EepPackUtil.removeNotSupportChar(FileNameUtil.getName(fileName));
            Path zipPath = Paths.get(this.properties.getDir().getParent(), fileName);
            File zip;
            try {
                zip = ZipUtil.zip(dir.getPath(), zipPath.toString(), false, this.properties.getCharset());
                return zip;
            } catch (IOException e) {
                throw new EepPackException("压缩文件出错：" + e.getMessage(), e);
            } finally {
                this.doClear();
            }
        } else {
            throw new IllegalArgumentException("文件名称不可为空！");
        }
    }

    private void doClear() {
        FileUtil.clean(this.properties.getDir());
        this.getCatalogue().getRecords().clear();
        this.statistic.clear();
    }
}
