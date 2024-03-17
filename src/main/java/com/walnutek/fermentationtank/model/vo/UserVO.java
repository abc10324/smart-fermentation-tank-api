package com.walnutek.fermentationtank.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.walnutek.fermentationtank.config.mongo.AggregationLookupBuilder;
import com.walnutek.fermentationtank.model.entity.BaseColumns;
import com.walnutek.fermentationtank.model.entity.Laboratory;
import com.walnutek.fermentationtank.model.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Schema(title = "使用者VO")
@Data
@EqualsAndHashCode(callSuper = false)
public class UserVO extends BaseColumns {

    @Schema(title = "帳號/Email")
    private String account;

    @Schema(title = "密碼", accessMode = AccessMode.WRITE_ONLY)
    @JsonProperty(access = Access.WRITE_ONLY)
    private String password;

    @Schema(title = "使用者類別", accessMode = AccessMode.READ_ONLY)
    private User.Role role;

    @Schema(title = "使用者名稱")
    private String name;

    @Schema(title = "Email")
    private String email;

    @Schema(title = "所屬實驗室ID")
    private List<String> labList = List.of();

    @Schema(title = "所屬實驗室名稱", accessMode = Schema.AccessMode.READ_ONLY)
    private List<String> labNameList = List.of();

    @Schema(title = "帳號狀態", accessMode = Schema.AccessMode.READ_ONLY)
    private User.Status status = User.Status.ACTIVE;

    @Schema(title = "登入次數", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer loginCount = 0;

    @Schema(title = "最後登入時間", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm")
    private LocalDateTime lastLoginTime;

    @Schema(title = "帳號狀態名稱", accessMode = Schema.AccessMode.READ_ONLY)
    public String getStatusName(){
        return Optional.ofNullable(status)
                .map(User.Status::getName)
                .orElse("未定義");
    }

    public static UserVO of(User data) {
        var vo = new UserVO();
        vo.account = data.getAccount();
        vo.role = data.getRole();
        vo.name = data.getName();
        vo.email = data.getEmail();
        vo.status = data.getStatus();
        vo.loginCount = data.getLoginCount();
        vo.lastLoginTime = data.getLastLoginTime();

        syncBaseColumns(data, vo);

        return vo;
    }

    @JsonIgnore
    public static List<AggregationOperation> getLookupAggregation() {
    	List<AggregationOperation> aggregationList = new ArrayList<>();
        aggregationList.addAll(getLaboratoryLookupAggregation());

    	return aggregationList;
    }

    private static List<AggregationOperation> getLaboratoryLookupAggregation() {
        return AggregationLookupBuilder.from(User.class)
                .outerJoin(Laboratory.class)
                .on(User::getLabList, Laboratory::getId)
                .mappingTo(UserVO.class)
                .asArrayField()
//                .mapping(Laboratory::getId, UserVO::getLabList)
                .mapping(Laboratory::getName, UserVO::getLabNameList)
                .build();
    }

}
