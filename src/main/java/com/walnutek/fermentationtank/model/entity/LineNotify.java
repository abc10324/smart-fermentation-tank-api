package com.walnutek.fermentationtank.model.entity;


import com.walnutek.fermentationtank.config.Const;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

/**
 * LineNotify
 * @author Walnutek-Sam
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Document(Const.COLLECTION_LINE_NOTIFY)
public class LineNotify extends BaseColumns {

    /**
     * 場域(實驗室Id)
     */
    @Field(targetType = FieldType.OBJECT_ID)
    private String laboratoryId;

    /**
     * Assigns the generated redirect URI
     */
    private String redirectUri;

    /**
     * A code for acquiring access tokens
     */
    private String code;

    /**
     * Directly sends the assigned state parameter
     */
    private String state;


    /**
     * access token
     */
    private String accessToken;

    /**
     * 使用者Id
     */
    @Field(targetType = FieldType.OBJECT_ID)
    private String userId;

    /**
     * 狀態
     */
    private Status status = Status.ACTIVE;
}
