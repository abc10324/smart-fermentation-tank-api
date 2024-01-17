package com.walnutek.fermentationtank.model.entity;

import com.walnutek.fermentationtank.config.Const;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

/**
 * 實驗室
 * @author Walnutek-Sam
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Document(Const.COLLECTION_LABORATORY)
public class Laboratory extends BaseColumns {

    /**
     * 實驗室管理者ID
     */
    @Field(targetType = FieldType.OBJECT_ID)
    private String ownerId;

    /**
     * 實驗室名稱
     */
    private String name;

    /**
     * 備註
     */
    private String note;

    /**
     * 實驗室狀態
     */
    private Status status = Status.ACTIVE;

}
