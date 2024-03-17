package com.walnutek.fermentationtank.model.entity;

import com.walnutek.fermentationtank.config.Const;
import com.walnutek.fermentationtank.model.entity.Device.DeviceType;
import com.walnutek.fermentationtank.model.vo.AlertVO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
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
     * 場域(實驗室Id)
     */
    @Field(targetType = FieldType.OBJECT_ID)
    private String laboratoryId;

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
    private Condition condition;

    /**
     * 閥值
     */
    private Double threshold;

    /**
     * 裝置分類
     */
    private DeviceType type = DeviceType.FERMENTER;

    /**
     * 狀態
     */
    private Status status = Status.ACTIVE;

    public Alert apply(AlertVO data){
        this.setName(data.getName());
        this.setType(data.getType());
        this.setDeviceId(data.getDeviceId());
        this.setCheckField(data.getCheckField());
        this.setCondition(data.getCondition());
        this.setThreshold(data.getThreshold());
        return this;
    }

    @Getter
    public enum Condition {
        GREATER_THAN( "大於"),

        LESS_THAN( "小魚");
        private String name;

        Condition(String name) {
            this.name = name;
        }
    }
}

