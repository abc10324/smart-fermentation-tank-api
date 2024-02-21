package com.walnutek.fermentationtank.model.vo;

import com.walnutek.fermentationtank.model.entity.BaseColumns;
import com.walnutek.fermentationtank.model.entity.LineNotify;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

import static com.walnutek.fermentationtank.model.entity.BaseColumns.syncBaseColumns;

@Schema(title = "LineNotifyVO")
@Data
@EqualsAndHashCode(callSuper = false)
public class LineNotifyVO extends BaseColumns {

    @Schema(title = "場域(實驗室Id)")
    private String laboratoryId;

    @Schema(title = "實驗室名稱")
    private String laboratory;

    @Schema(title = "lineId")
    private String lineId;

    @Schema(title = "使用者名稱")
    private String name;

    @Schema(title = "狀態")
    private BaseColumns.Status status;

    @Schema(title = "建立時間")
    private LocalDateTime createTime;

    public static LineNotifyVO of(LineNotify data, String labName) {
        var vo = new LineNotifyVO();
        vo.laboratoryId = data.getLaboratoryId();
        vo.laboratory = labName;
        vo.lineId = data.getLineId();
        vo.name = data.getName();
        vo.status = data.getStatus();
        syncBaseColumns(data, vo);

        return vo;
    }

}
