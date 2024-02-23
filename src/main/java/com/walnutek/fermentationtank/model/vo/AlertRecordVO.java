package com.walnutek.fermentationtank.model.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.walnutek.fermentationtank.config.mongo.AggregationLookupBuilder;
import com.walnutek.fermentationtank.model.entity.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;

import java.util.ArrayList;
import java.util.List;

@Schema(title = "警報紀錄VO")
@Data
@EqualsAndHashCode(callSuper = false)
public class AlertRecordVO {

    @Schema(title = "實驗室Id")
    private String laboratoryId;

    @Schema(title = "警報Id")
    private String alertId;

    @Schema(title = "警報名稱")
    private String name;

    @Schema(title = "目標裝置Id")
    private String deviceId;

    @Schema(title = "目標裝置名稱")
    private String device;

    @Schema(title = "目標欄位")
    private String checkField;

    @Schema(title = "觸發警報數值")
    private Double triggerValue;

    @Schema(title = "警報狀態")
    private AlertRecord.AlertState state;

    @Schema(title = "備註")
    private String note;

    public static AlertRecordVO of(AlertRecord alertRecord, Alert alert, String device, String laboratory) {
        var vo = new AlertRecordVO();
        vo.laboratoryId = alert.getLaboratoryId();
        vo.alertId = alertRecord.getAlertId();
        vo.name = alert.getName();
        vo.deviceId = alert.getDeviceId();
        vo.device = device;
        vo.checkField = alert.getCheckField();
        vo.triggerValue = alertRecord.getTriggerValue();
        vo.state = alertRecord.getState();
        vo.note = alertRecord.getNote();
        return vo;
    }

    @JsonIgnore
    public static List<AggregationOperation> getLookupAggregation() {
        List<AggregationOperation> aggregationList = new ArrayList<>();
        aggregationList.addAll(getAlertLookupAggregation());
        aggregationList.addAll(getDeviceLookupAggregation());

        return aggregationList;
    }

    private static List<AggregationOperation> getAlertLookupAggregation() {
        return AggregationLookupBuilder.from(AlertRecord.class)
                .outerJoin(Alert.class)
                .on(AlertRecord::getAlertId, Alert::getId)
                .mappingTo(AlertRecordVO.class)
                .asArrayField()
                .mapping(Alert::getName, AlertRecordVO::getName)
                .mapping(Alert::getDeviceId, AlertRecordVO::getDeviceId)
                .mapping(Alert::getCheckField, AlertRecordVO::getCheckField)
                .build();
    }

    private static List<AggregationOperation> getDeviceLookupAggregation() {
        return AggregationLookupBuilder.from(AlertRecord.class)
                .outerJoin(Device.class)
                .on(AlertRecord::getDeviceId, Device::getId)
                .mappingTo(AlertRecordVO.class)
                .asArrayField()
                .mapping(Device::getName, AlertRecordVO::getDevice)
                .build();
    }
}
