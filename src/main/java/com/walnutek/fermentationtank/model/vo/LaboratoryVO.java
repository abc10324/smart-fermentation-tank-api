package com.walnutek.fermentationtank.model.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.walnutek.fermentationtank.config.mongo.AggregationLookupBuilder;
import com.walnutek.fermentationtank.model.entity.BaseColumns;
import com.walnutek.fermentationtank.model.entity.Laboratory;
import com.walnutek.fermentationtank.model.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;

import java.util.ArrayList;
import java.util.List;

import static com.walnutek.fermentationtank.config.Const.LOOKUP_COLLECTION_USER;

@Schema(title = "實驗室VO")
@Data
@EqualsAndHashCode(callSuper = false)
public class LaboratoryVO extends BaseColumns {

    @Schema(title = "實驗室管理者ID")
    private String ownerId;

    @Schema(title = "實驗室名稱")
    private String name;

    @Schema(title = "備註")
    private String note;

    @Schema(title = "實驗室狀態")
    private Status status = Status.ACTIVE;

    @Schema(title = "實驗室人員清單")
    private List<UserVO> memberList;

    public static LaboratoryVO of(Laboratory data, List<UserVO> memberList) {
        var vo = new LaboratoryVO();
        vo.ownerId = data.getOwnerId();
        vo.name = data.getName();
        vo.note = data.getNote();
        vo.status = data.getStatus();
        vo.memberList = memberList;

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
        return AggregationLookupBuilder.from(Laboratory.class)
                .outerJoin(User.class)
                .on(Laboratory::getId, User::getLabList)
                .mappingTo(LaboratoryVO.class)
                .as(LOOKUP_COLLECTION_USER)
                .asArrayField()
                .mapping(LaboratoryVO::getMemberList)
                .build();
    }

}
