package com.walnutek.fermentationtank.model.entity;


import com.walnutek.fermentationtank.config.Const;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

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
     * LineId
     */
    private String lineId;

    /**
     * 名稱
     */
    private String name;

    /**
     * 狀態
     */
    private Status status = Status.ACTIVE;
}
