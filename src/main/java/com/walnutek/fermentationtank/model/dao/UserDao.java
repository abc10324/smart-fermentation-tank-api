package com.walnutek.fermentationtank.model.dao;

import com.walnutek.fermentationtank.config.auth.AuthUser;
import com.walnutek.fermentationtank.config.mongo.CriteriaBuilder;
import com.walnutek.fermentationtank.config.mongo.UpdateBuilder;
import com.walnutek.fermentationtank.model.entity.User;
import com.walnutek.fermentationtank.model.vo.Page;
import com.walnutek.fermentationtank.model.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

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
        var criteriaList = Stream.of(
//                where(User::getRole).ne(User.Role.SUPER_ADMIN),
                where(hasText(paramMap.get("keyword")), UserVO::getAccount).like(paramMap.get("keyword"))
                        .or(where(UserVO::getName).like(paramMap.get("keyword")))
                        .or(where(UserVO::getEmail).like(paramMap.get("keyword")))
            ).map(CriteriaBuilder::build)
            .filter(Objects::nonNull)
            .toList();

        var sort = getSort(paramMap);
        var pageable = getPageable(paramMap);

        return QueryCondition.of(criteriaList, sort, pageable);
    }
    
    public boolean isAccountExist(String account) {
        var query = new Query();
        query.addCriteria(where(User::getAccount).is(account).build());

        return template.exists(query, User.class);
    }

    public AuthUser getLoginUser(String account, String password) {
        var query = new Query();
        query.addCriteria(where(User::getAccount).is(account).build());
        query.addCriteria(where(User::getPassword).is(password).build());
        
        return Optional.ofNullable(template.findOne(query, User.class))
                       .map(AuthUser::of)
                       .orElse(null);
    }

    public AuthUser getLoginUserInfo(String userId) {
        return Optional.ofNullable(selectById(userId))
                       .map(AuthUser::of)
                       .orElse(null);
    }

    public void addLoginCount(String userId) {
    	var update = UpdateBuilder.newInstance()
    					.inc(User::getLoginCount, 1)
    					.build();
        updateById(userId, update);
    }

}
