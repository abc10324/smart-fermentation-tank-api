package com.walnutek.fermentationtank.model.entity;

import com.walnutek.fermentationtank.config.Const;
import com.walnutek.fermentationtank.model.vo.AlertRecordVO;
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
     * 場域(實驗室Id)
     */
    @Field(targetType = FieldType.OBJECT_ID)
    private String laboratoryId;

    /**
     * 警報Id
     */
    @Field(targetType = FieldType.OBJECT_ID)
    private String alertId;

    /**
     * 目標裝置(感應器Id)
     */
    @Field(targetType = FieldType.OBJECT_ID)
    private String deviceId;

    /**
     * 觸發警報數值
     */
    private Double triggerValue;

    /**
     * 狀態
     */
    private AlertState state = AlertState.ISSUE;

    /**
     * 備註
     */
    private String note;

    public AlertRecord apply(AlertRecordVO data){
        this.setLaboratoryId(data.getLaboratoryId());
        this.setAlertId(data.getAlertId());
        this.setDeviceId(data.getDeviceId());
        this.setTriggerValue(data.getTriggerValue());
        this.setState(data.getState());
        this.setNote(data.getNote());

        return this;
    }

    public AlertRecord apply(Alert data, Double triggerValue){
        this.setLaboratoryId(data.getLaboratoryId());
        this.setAlertId(data.getId());
        this.setDeviceId(data.getDeviceId());
        this.setTriggerValue(triggerValue);
        this.setState(AlertRecord.AlertState.ISSUE);

        return this;
    }

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
