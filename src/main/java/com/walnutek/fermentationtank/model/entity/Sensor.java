package com.walnutek.fermentationtank.model.entity;

import com.walnutek.fermentationtank.config.Const;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.util.List;

/**
 * 感應器
 * @author Walnutek-Sam
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Document(Const.COLLECTION_SENSOR)
public class Sensor extends BaseColumns {

    /**
     * 場域(實驗室Id)
     */
    @Field(targetType = FieldType.OBJECT_ID)
    private String laboratoryId;

    /**
     * 裝置ID
     */
    @Field(targetType = FieldType.OBJECT_ID)
    private String deviceId;

    /**
     * 標籤
     */
    private String label;

    /**
     * 目標欄位列表
     */
    private List<String> checkFieldList;
}
