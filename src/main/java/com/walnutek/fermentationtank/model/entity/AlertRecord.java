package com.walnutek.fermentationtank.model.entity;

import com.walnutek.fermentationtank.config.Const;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

/**
 * 警報紀錄
 * @author Walnutek-Sam
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Document(Const.COLLECTION_ALERT_RECORD)
public class AlertRecord extends BaseColumns {

    /**
     * 警報Id
     */
    @Field(targetType = FieldType.OBJECT_ID)
    private String alertId;

    /**
     * 狀態
     */
    private AlertState state = AlertState.ISSUE;

    /**
     * 備註
     */
    private String note;

    @Getter
    public enum AlertState {
        ISSUE( "發布"),
        LIFT( "解除");
        private String name;

        AlertState(String name) {
            this.name = name;
        }
    }
}
