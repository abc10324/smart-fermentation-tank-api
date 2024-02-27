package com.walnutek.fermentationtank.model.vo;

import com.walnutek.fermentationtank.model.entity.BaseColumns;
import com.walnutek.fermentationtank.model.entity.Sensor;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    private List<SensorRecordVO> uploadList;

    public Sensor toSensor(Sensor data) {
        data.setLaboratoryId(laboratoryId);
        data.setDeviceId(deviceId);
        data.setLabel(label);
        List<String> checkFieldList = new ArrayList<>();
        if(!uploadList.isEmpty()){
            var uploadData = uploadList.get(0).getUploadData();
            if (Objects.nonNull(uploadData)){
                checkFieldList = uploadData.keySet().stream().toList();
            }
        }
        data.setCheckFieldList(checkFieldList);

        updateBaseColumns(this, data);

        return data;
    }
}
