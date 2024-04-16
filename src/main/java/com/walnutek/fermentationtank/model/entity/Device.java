package com.walnutek.fermentationtank.model.entity;

import com.walnutek.fermentationtank.config.Const;
import com.walnutek.fermentationtank.model.vo.DeviceVO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

/**
 * 裝置
 * @author Walnutek-Sam
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Document(Const.COLLECTION_DEVICE)
public class Device extends BaseColumns {

    /**
     * 裝置名稱
     */
    private String name;

    /**
     * 場域(實驗室Id)
     */
    @Field(targetType = FieldType.OBJECT_ID)
    private String laboratoryId;

    /**
     * 裝置分類
     */
    private DeviceType type = DeviceType.FERMENTER;


    private Status status = Status.ACTIVE;

    public Device apply(DeviceVO data){
        this.setName(data.getName());
        this.setLaboratoryId(data.getLaboratoryId());
        this.setType(data.getType());
        return this;
    }

    @Getter
    public enum DeviceType {
        ALL("全部"),
        FERMENTER( "醱酵槽"),
        BSC("生物安全櫃");
        private String name;

        DeviceType(String name) {
            this.name = name;
        }
    }
}
