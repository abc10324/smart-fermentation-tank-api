package com.walnutek.fermentationtank.model.entity;

import com.walnutek.fermentationtank.config.Const;
import com.walnutek.fermentationtank.model.vo.SensorRecordVO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

/**
 * 感應器紀錄
 * @author Walnutek-Sam
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Document(Const.COLLECTION_SENSOR_RECORD)
public class SensorRecord extends BaseColumns {

    /**
     * 感應器Id
     */
    @Field(targetType = FieldType.OBJECT_ID)
    private String sensorId;

    private Long recordTime;

    /**
     * 資料
     */
    private org.bson.Document uploadData;

    public SensorRecord apply(SensorRecordVO data){
        this.setSensorId(data.getSensorId());
        this.setRecordTime(data.getRecordTime());
        this.setUploadData(data.getUploadData());
        return this;
    }
}
