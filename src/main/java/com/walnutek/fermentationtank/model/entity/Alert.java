package com.walnutek.fermentationtank.model.entity;

import com.walnutek.fermentationtank.config.Const;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

/**
 * 警報管理
 * @author Walnutek-Sam
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Document(Const.COLLECTION_ALERT)
public class Alert extends BaseColumns {

    /**
     * 警報名稱
     */
    private String name;

    /**
     * 目標裝置(感應器Id)
     */
    @Field(targetType = FieldType.OBJECT_ID)
    private String deviceId;

    /**
     * 目標欄位
     */
    private String checkField;

    /**
     * 條件
     */
    private String condition;

    /**
     * 閥值
     */
    private Double threshold;

    /**
     * 裝置分類
     */
    private Sensor.SensorType type = Sensor.SensorType.FERMENTER;

    /**
     * 狀態
     */
    private Status status = Status.ACTIVE;
}
