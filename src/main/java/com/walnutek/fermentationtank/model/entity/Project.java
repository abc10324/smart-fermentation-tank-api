package com.walnutek.fermentationtank.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.walnutek.fermentationtank.config.Const;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.time.LocalDateTime;

/**
 * 專案
 * @author Walnutek-Sam
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Document(Const.COLLECTION_PROJECT)
public class Project extends BaseColumns {

    /**
     * 場域(實驗室Id)
     */
    @Field(targetType = FieldType.OBJECT_ID)
    private String laboratoryId;

    /**
     * 目標裝置ID
     */
    @Field(targetType = FieldType.OBJECT_ID)
    private String deviceId;

    /**
     * 專案名稱
     */
    private String name;

    /**
     * 起始時間
     */
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm")
    private LocalDateTime startTime;

    /**
     * 結束時間
     */
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm")
    private LocalDateTime endTime;

    /**
     * 專案狀態
     */
    private Status status = Status.ACTIVE;

}
