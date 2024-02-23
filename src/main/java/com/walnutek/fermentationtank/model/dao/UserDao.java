package com.walnutek.fermentationtank.model.dao;

import com.walnutek.fermentationtank.config.Const;
import com.walnutek.fermentationtank.config.auth.Auth;
import com.walnutek.fermentationtank.config.mongo.CriteriaBuilder;
import com.walnutek.fermentationtank.exception.AppException;
import com.walnutek.fermentationtank.model.entity.BaseColumns;
import com.walnutek.fermentationtank.model.entity.User;
import com.walnutek.fermentationtank.model.vo.Page;
import com.walnutek.fermentationtank.model.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static com.walnutek.fermentationtank.config.mongo.CriteriaBuilder.where;
import static com.walnutek.fermentationtank.model.service.Utils.hasText;

@Repository
public class UserDao extends BaseDao<User> {

    @Autowired
    private MongoTemplate template;

    public Page<UserVO> search(Map<String, Object> paramMap){
        return aggregationSearch(getQueryCondition(paramMap), UserVO.class);
    }

    private QueryCondition getQueryCondition(Map<String,Object> paramMap) {
        var loginUser = Auth.getAuthUser();
        var criteriaList = Stream.of(
                where(User::getRole).ne(User.Role.SUPER_ADMIN),
                where(User::getStatus).is(BaseColumns.Status.ACTIVE),
                where(User.Role.LAB_ADMIN.equals(loginUser.getRole()), User::getRole).is(User.Role.LAB_USER),
                where(hasText(paramMap.get(Const.KEYWORD)), UserVO::getAccount).like(paramMap.get(Const.KEYWORD))
                        .or(where(UserVO::getName).like(paramMap.get(Const.KEYWORD)))
                        .or(where(UserVO::getEmail).like(paramMap.get(Const.KEYWORD)))
            ).map(CriteriaBuilder::build)
            .filter(Objects::nonNull)
            .toList();

        if(User.Role.LAB_ADMIN.equals(loginUser.getRole())) {
            criteriaList = new ArrayList<>(criteriaList);
            criteriaList.addAll(Stream.of(
                where(User::getRole).is(User.Role.LAB_USER),
                where(User::getAdminId).is(loginUser.getUserId())
            ).map(CriteriaBuilder::build)
            .toList());
        }

        var sort = getSort(paramMap);
        var pageable = getPageable(paramMap);

        return QueryCondition.of(criteriaList, sort, pageable);
    }

    public boolean isAccountExist(String account) {
        var query = new Query();
        query.addCriteria(where(User::getAccount).is(account).build());

        return template.exists(query, User.class);
    }

    public User getUserByAccountAndPassword(String account, String password) {
        var query = new Query();
        query.addCriteria(where(User::getAccount).is(account).build());
        query.addCriteria(where(User::getPassword).is(password).build());
        query.addCriteria(where(User::getStatus).is(BaseColumns.Status.ACTIVE).build());

        return template.findOne(query, User.class);
    }

    public User userValidCheckAndGetUserInfo(String userId) {
        var user = Optional.ofNullable(selectById(userId))
                .orElseThrow(() -> new AppException(AppException.Code.E004));
        if(User.Status.DELETED.equals(user.getStatus())) {
            throw new AppException(AppException.Code.E001, "帳號已刪除");
        }
        return user;
    }
}
