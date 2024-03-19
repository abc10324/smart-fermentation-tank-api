package com.walnutek.fermentationtank.model.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.walnutek.fermentationtank.config.mongo.AggregationLookupBuilder;
import com.walnutek.fermentationtank.model.entity.BaseColumns;
import com.walnutek.fermentationtank.model.entity.LineNotify;
import com.walnutek.fermentationtank.model.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;

import java.util.ArrayList;
import java.util.List;

import static com.walnutek.fermentationtank.config.Const.LOOKUP_COLLECTION_USER;

@Schema(title = "LineNotifyVO")
@Data
@EqualsAndHashCode(callSuper = false)
public class LineNotifyVO extends BaseColumns {

    @Schema(title = "場域(實驗室Id)")
    private String laboratoryId;

    @Schema(title = "實驗室名稱")
    private String laboratory;

//    @Schema(title = "lineId")
//    private String lineId;

    @Schema(title = "使用者名稱")
    private String userName;

    @Schema(title = "狀態")
    private BaseColumns.Status status;

    public static LineNotifyVO of(LineNotify data, String labName) {
        var vo = new LineNotifyVO();
        vo.laboratoryId = data.getLaboratoryId();
        vo.laboratory = labName;
        vo.status = data.getStatus();
        syncBaseColumns(data, vo);

        return vo;
    }

    @JsonIgnore
    public static List<AggregationOperation> getLookupAggregation() {
        List<AggregationOperation> aggregationList = new ArrayList<>();
        aggregationList.addAll(getUserLookupAggregation());

        return aggregationList;
    }

    private static List<AggregationOperation> getUserLookupAggregation() {
        return AggregationLookupBuilder.from(LineNotify.class)
                .outerJoin(User.class)
                .on(LineNotify::getUserId, User::getId)
                .mappingTo(LineNotifyVO.class)
                .as(LOOKUP_COLLECTION_USER)
                .asArrayField()
                .mapping(User::getName, LineNotifyVO::getUserName)
                .build();
    }

}
