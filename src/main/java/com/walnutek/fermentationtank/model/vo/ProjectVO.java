package com.walnutek.fermentationtank.model.vo;

import com.walnutek.fermentationtank.model.entity.BaseColumns;
import com.walnutek.fermentationtank.model.entity.Project;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Schema(title = "專案VO")
@Data
@EqualsAndHashCode(callSuper = false)
public class ProjectVO extends BaseColumns {

    @Schema(title = "場域(實驗室Id)")
    private String laboratoryId;

    @Schema(title = "實驗室名稱")
    private String laboratory;

    @Schema(title = "專案名稱")
    private String name;

    @Schema(title = "目標裝置ID")
    private String deviceId;

    @Schema(title = "目標裝置名稱")
    private String device;

    @Schema(title = "起始時間")
    private LocalDateTime startTime;

    @Schema(title = "結束時間")
    private LocalDateTime endTime;

    public static ProjectVO of(Project data, String labName, String device) {
        var vo = new ProjectVO();
        vo.laboratoryId = data.getLaboratoryId();
        vo.laboratory = labName;
        vo.name = data.getName();
        vo.deviceId = data.getDeviceId();
        vo.device = device;
        vo.startTime = data.getStartTime();
        vo.endTime = data.getEndTime();
        syncBaseColumns(data, vo);

        return vo;
    }

}
