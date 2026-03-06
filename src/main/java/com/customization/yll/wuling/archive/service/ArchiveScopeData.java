package com.customization.yll.wuling.archive.service;

import lombok.ToString;

/**
  * @desc 归档范围数据
  * @author 姚礼林
  * @date 2025/6/6
 **/
@ToString
public class ArchiveScopeData {
   private String keyword;
   private String categoryNumber;
   private String saveYears;

    public ArchiveScopeData() {
    }

    public ArchiveScopeData(String keyword, String categoryNumber, String saveYears) {
        this.keyword = keyword;
        this.categoryNumber = categoryNumber;
        this.saveYears = saveYears;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
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
