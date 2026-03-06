package com.customization.yll.wuling.archive.util;

import com.customization.yll.common.mode.conf.entity.ParamConfigurationEntity;
import com.customization.yll.wuling.archive.entity.FileMetadataConfEntity;
import lombok.experimental.UtilityClass;

/**
 * @author 姚礼林
 * @desc 对象类型转换工具类
 * @date 2025/6/9
 **/
@UtilityClass
public class ObjectConvertUtil {

    public ParamConfigurationEntity convertToParamConfigurationEntity(FileMetadataConfEntity entity) {
        ParamConfigurationEntity paramConfigurationEntity = new ParamConfigurationEntity();
        paramConfigurationEntity.setName(entity.getName());
        paramConfigurationEntity.setRequired(entity.isRequired());
        paramConfigurationEntity.setSysParam(entity.getSystemParam());
        paramConfigurationEntity.setDefaultValue(entity.getDefaultValue());
        paramConfigurationEntity.setFixedValue(entity.getFixValue());
        paramConfigurationEntity.setGetWorkflowFieldDataWay(entity.getGetWorkflowFieldDataWay());
        paramConfigurationEntity.setCodeFixed(entity.isFixed());
        paramConfigurationEntity.setWorkflowFieldId(entity.getWorkflowFieldId());
        return paramConfigurationEntity;
    }
}
