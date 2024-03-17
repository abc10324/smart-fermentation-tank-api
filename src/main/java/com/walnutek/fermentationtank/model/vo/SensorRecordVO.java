package com.walnutek.fermentationtank.model.vo;

import com.walnutek.fermentationtank.model.entity.BaseColumns;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

@Schema(title = "SensorRecordVO")
@Data
@EqualsAndHashCode(callSuper = false)
public class SensorRecordVO extends BaseColumns {
    @Schema(title = "感應器Id")
    @Field(targetType = FieldType.OBJECT_ID)
    private String sensorId;

    @Schema(title = "recordTime")
    private Long recordTime;

    @Schema(title = "資料")
    private Document uploadData;
}
