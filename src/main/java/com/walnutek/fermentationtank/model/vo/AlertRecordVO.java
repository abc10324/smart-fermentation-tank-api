package com.walnutek.fermentationtank.model.vo;

import com.walnutek.fermentationtank.model.entity.Alert;
import com.walnutek.fermentationtank.model.entity.AlertRecord;
import com.walnutek.fermentationtank.model.entity.Fermenter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(title = "警報紀錄VO")
@Data
@EqualsAndHashCode(callSuper = false)
public class AlertRecordVO {

    @Schema(title = "警報Id")
    private String alertId;

    @Schema(title = "警報名稱")
    private String name;

    @Schema(title = "目標裝置Id")
    private String deviceId;

    @Schema(title = "目標裝置名稱")
    private String device;

    @Schema(title = "實驗室Id")
    private String laboratoryId;

    @Schema(title = "實驗室名稱")
    private String laboratory;

    @Schema(title = "目標欄位")
    private String checkField;

    @Schema(title = "警報狀態")
    private AlertRecord.AlertState state;

    public static AlertRecordVO of(AlertRecord alertRecord, Alert alert, String device, String laboratory) {
        var vo = new AlertRecordVO();
        vo.alertId = alertRecord.getAlertId();
        vo.name = alert.getName();
        vo.deviceId = alert.getDeviceId();
        vo.device = device;
        vo.laboratoryId = alert.getLaboratoryId();
        vo.laboratory = laboratory;
        vo.checkField = alert.getCheckField();
        vo.state = alertRecord.getState();
        return vo;
    }
}
