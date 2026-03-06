package com.customization.yll.wuling.archive.data;

import cn.hutool.core.util.StrUtil;
import com.customization.yll.common.exception.ConfigModeDataNotFoundException;
import com.customization.yll.common.mode.conf.ParamConfManager;
import com.customization.yll.common.workflow.WorkflowFieldValueManager;
import com.customization.yll.wuling.archive.config.ArchiveConfig;
import com.customization.yll.wuling.archive.config.ConfigurationModeDataManager;
import com.customization.yll.wuling.archive.entity.FileMetadataConfEntity;
import com.customization.yll.wuling.archive.service.ArchiveScopeValidator;
import com.customization.yll.wuling.archive.service.ValidateResult;
import com.customization.yll.wuling.archive.util.ObjectConvertUtil;
import com.engine.email.biz.Html2Text;
import lombok.Getter;
import weaver.integration.logging.Logger;
import weaver.integration.logging.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 获取文件实体元数据抽象类
 * @author yaolilin
 */
abstract class AbstractFileMetadataManager {
    private Map<String ,String > fileMetadataEntity;
    private final ConfigurationModeDataManager configurationModeDataManager;
    private final ParamConfManager paramConfManager;
    private final ArchiveScopeValidator archiveScopeValidator = new ArchiveScopeValidator();
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    @Getter
    private final Integer requestId;

    protected AbstractFileMetadataManager(ConfigurationModeDataManager configurationModeDataManager,
                                          WorkflowFieldValueManager fieldValueManager, Integer requestId) {
        this.configurationModeDataManager = configurationModeDataManager;
        this.requestId = requestId;
        this.paramConfManager = new ParamConfManager(fieldValueManager);
    }

    /**
     * 获取业务实体元数据，根据配置建模中配置文件实体元数据的明细，获取元数据值
     * @return 业务实体元数据
     */
    Map<String ,String > getMetadata() {
        if (fileMetadataEntity != null) {
            return fileMetadataEntity;
        }
        fileMetadataEntity = new HashMap<>(40);
        List<FileMetadataConfEntity> fileMetadataConfDetail = configurationModeDataManager.getFileMetadataConfDetail();
        for (FileMetadataConfEntity entity : fileMetadataConfDetail) {
            // 是否为代码固定，如果为代码固定，需要通过后端代码对元数据进行赋值，否则通过建模中的配置取值
            String value;
            if (entity.isFixed()) {
                value = putCodeFixedValue(entity);
            } else {
                value = paramConfManager
                        .getParamValue(ObjectConvertUtil.convertToParamConfigurationEntity(entity));
            }
            if (StrUtil.isNotBlank(value)) {
                value = Html2Text.getContent(value);
            }
            fileMetadataEntity.put(entity.getName(), value);
        }
        if (fileMetadataEntity.containsKey("日期")) {
            String date = fileMetadataEntity.get("日期");
            if (date.contains("-")) {
                fileMetadataEntity.put("日期", date.replace("-", ""));
            }
        }
        if (ArchiveConfig.enableArchivingScope()) {
            validateArchiveScope();
        }
        return fileMetadataEntity;
    }

    /**
     * 对需要在后端代码中赋值的元数据进行赋值。在建模配置的元数据有些是无法通过配置取值的，需要在后端代码生成，通过传入的参数 entity 判断
     * 是否为需要在代码赋值的元数据。
     *
     * @param entity 建模中配置的元数据，需要根据此元数据判断是否需要代码赋值，可通过 {@link FileMetadataConfEntity#getName()} ()}
     *               判断是否为需要代码赋值的元数据
     */
    abstract String putCodeFixedValue(FileMetadataConfEntity entity);

    /**
     * 调用档案系统提供的归档范围接口，校验是否在归档范围，并从中获取分类号和保密期限
     */
    private void validateArchiveScope() {
        if (fileMetadataEntity.containsKey("题名")) {
            ValidateResult result = archiveScopeValidator.validate(fileMetadataEntity.get("题名"));
            log.info("归档范围校验结果：" + result);
            if (result.isInScope()) {
                fileMetadataEntity.put("分类号", result.getCategoryNumber());
                fileMetadataEntity.put("保管期限", result.getSaveYears());
            }
        }else {
            throw new ConfigModeDataNotFoundException("配置建模的文件实体元数据中未找到【题名】元数据配置，无法校验是否需要归档");
        }
    }

}
