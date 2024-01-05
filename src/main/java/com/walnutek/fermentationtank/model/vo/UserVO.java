package com.walnutek.fermentationtank.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.walnutek.fermentationtank.config.mongo.AggregationLookupBuilder;
import com.walnutek.fermentationtank.model.entity.BaseColumns;
import com.walnutek.fermentationtank.model.entity.Laboratory;
import com.walnutek.fermentationtank.model.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Schema(title = "使用者VO")
@Data
@EqualsAndHashCode(callSuper = false)
public class UserVO extends BaseColumns {

    @Schema(title = "帳號/Email")
    private String account;

    @Schema(title = "密碼", accessMode = Schema.AccessMode.WRITE_ONLY)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Schema(title = "使用者類別", accessMode = Schema.AccessMode.READ_ONLY)
    private User.Role role;

    @Schema(title = "使用者名稱")
    private String name;

    @Schema(title = "Email")
    private String email;

//    @JsonIgnore
    private Laboratory[] labArr = new Laboratory[]{};

    public List<String> getLabIdList() {
        List<String> resultList = new ArrayList<>();

        if(Objects.nonNull(labArr)) {
            System.out.println(labArr);
            Stream.of(labArr).forEach(System.out::println);
//            Stream.of(labArr).map(Laboratory::getId).forEach(resultList::add);
        }

        return resultList;
    }

    @Schema(title = "所屬實驗室ID", accessMode = Schema.AccessMode.WRITE_ONLY)
    private List<String> labIdList = List.of();

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

    public User toUser() {
        var data = new User();
        data.setAccount(account);
        data.setPassword(password);
        data.setName(name);
        data.setEmail(email);

        if(Objects.nonNull(labIdList)) {
            data.setLabList(labIdList.stream().map(ObjectId::new).toList());
        }

        data.setRole(role);
        data.setStatus(status);
        data.setLoginCount(loginCount);
        data.setLastLoginTime(lastLoginTime);

        syncBaseColumns(this, data);

        return data;
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
                .mapping(UserVO::getLabArr)
                .build();
    }
    
}
