package com.customization.yll.wuling.archive.service;

import lombok.ToString;

/**
 * @author 姚礼林
 * @desc 归档范围验证结果
 * @date 2025/6/6
 **/
@ToString
public class ValidateResult {
    private boolean isInScope;
    private String categoryNumber;
    private String saveYears;

    public ValidateResult(boolean isInScope, String categoryNumber, String saveYears) {
        this.isInScope = isInScope;
        this.categoryNumber = categoryNumber;
        this.saveYears = saveYears;
    }

    public boolean isInScope() {
        return isInScope;
    }

    public void setInScope(boolean inScope) {
        isInScope = inScope;
    }

    public String getCategoryNumber() {
        return categoryNumber;
    }

    public void setCategoryNumber(String categoryNumber) {
        this.categoryNumber = categoryNumber;
    }

    public String getSaveYears() {
        return saveYears;
    }

    public void setSaveYears(String saveYears) {
        this.saveYears = saveYears;
    }
}
