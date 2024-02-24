package com.walnutek.fermentationtank.model.vo;

import com.walnutek.fermentationtank.model.entity.BaseColumns;
import com.walnutek.fermentationtank.model.entity.Sensor;
import com.walnutek.fermentationtank.model.entity.SensorRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

@Schema(title = "SensorVO")
@Data
@EqualsAndHashCode(callSuper = false)
public class SensorVO extends BaseColumns {

    @Schema(title = "sensorId")
    @Field(targetType = FieldType.OBJECT_ID)
    private String id;

    @Schema(title = "場域(實驗室Id)")
    @Field(targetType = FieldType.OBJECT_ID)
    private String laboratoryId;

    @Schema(title = "裝置ID")
    @Field(targetType = FieldType.OBJECT_ID)
    private String deviceId;

    @Schema(title = "標籤")
    private String label;

    @Schema(title = "資料")
    private org.bson.Document uploadData;

    public Sensor toSensor(Sensor data) {
        data.setLaboratoryId(laboratoryId);
        data.setDeviceId(deviceId);
        data.setLabel(label);
        data.setCheckFieldList(uploadData.keySet().stream().toList());

        syncBaseColumns(this, data);

        return data;
    }

    public SensorRecord toSensorRecord(SensorRecord data) {
        data.setSensorId(id);
        data.setUploadData(uploadData);

        syncBaseColumns(this, data);

        return data;
    }
}
