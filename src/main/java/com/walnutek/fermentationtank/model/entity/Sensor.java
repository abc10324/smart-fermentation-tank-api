package com.walnutek.fermentationtank.model.entity;

import com.walnutek.fermentationtank.config.Const;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;

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
     * 對應類型
     */
    private SensorType type = SensorType.FERMENTER;

    @Getter
    public enum SensorType {
        FERMENTER( "醱酵槽");
        private String name;

        SensorType(String name) {
            this.name = name;
        }
    }
}
