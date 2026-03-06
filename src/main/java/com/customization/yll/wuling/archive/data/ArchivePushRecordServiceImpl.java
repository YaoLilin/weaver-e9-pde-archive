package com.customization.yll.wuling.archive.data;

import cn.hutool.core.convert.Convert;
import com.customization.yll.common.exception.SqlExecuteException;
import com.customization.yll.wuling.archive.constants.ArchiveStatus;
import com.customization.yll.wuling.archive.entity.ArchivePushRecordEntity;
import lombok.Setter;
import weaver.conn.RecordSet;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 姚礼林
 * @desc 档案推送记录, 读取建模表数据
 * @date 2025/9/30
 **/
@Setter
public class ArchivePushRecordServiceImpl implements ArchivePushRecordService {
    private final RecordSet recordSet;
    private String tableName = "uf_record_result";

    public ArchivePushRecordServiceImpl(RecordSet recordSet) {
        this.recordSet = recordSet;
    }

    @Override
    public List<ArchivePushRecordEntity> getNotFeedbackRecord() {
        String sql = "SELECT source_id,pushed,status,msg,feedback_date,workflow FROM " + tableName +
                " WHERE status in(?,?)";
        if (!recordSet.executeQuery(sql, ArchiveStatus.WAITING, ArchiveStatus.CHECK_PASSED)) {
            throw new SqlExecuteException("查询档案推送记录出错");
        }
        List<ArchivePushRecordEntity> records = new ArrayList<>();
        while (recordSet.next()) {
            ArchivePushRecordEntity entity = new ArchivePushRecordEntity();
            entity.setSourceId(recordSet.getString("source_id"));
            entity.setPushed(Convert.toBool(recordSet.getString("pushed")));
            entity.setStatus(Convert.toInt(recordSet.getString("status")));
            entity.setMsg(recordSet.getString("msg"));
            entity.setFeedbackDate(recordSet.getString("feedback_date"));
            entity.setRequestId(Convert.toInt(recordSet.getString("workflow")));
            records.add(entity);
        }
        return records;
    }
}
