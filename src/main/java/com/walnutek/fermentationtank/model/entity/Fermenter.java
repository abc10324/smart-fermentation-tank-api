package com.walnutek.fermentationtank.model.entity;

import com.walnutek.fermentationtank.config.Const;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

/**
 * 醱酵槽
 * @author Walnutek-Sam
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Document(Const.COLLECTION_FERMENTER)
public class Fermenter extends BaseColumns {

    /**
     * 感應器 Id
     */
    @Field(targetType = FieldType.OBJECT_ID)
    private String deviceId;

    /**
     * 醱酵槽名稱
     */
    private String name;

    /**
     * 場域(實驗室Id)
     */
    @Field(targetType = FieldType.OBJECT_ID)
    private String laboratoryId;

    /**
     * 連線狀態
     */
    private ConnectionStatus connectionStatus = ConnectionStatus.NORMAL;

    /**
     * MAC ADDRESS
     */
    private String macAddress;
    /**
     * 醱酵槽狀態
     */
    private Status status = Status.ACTIVE;

    @Getter
    public enum ConnectionStatus {
        NORMAL( "正常"),
        EXCEPTION("異常");

        private String name;

        ConnectionStatus(String name) {
            this.name = name;
        }
    }
}
