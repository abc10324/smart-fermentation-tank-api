package com.walnutek.fermentationtank.model.dao;

import com.walnutek.fermentationtank.config.auth.AuthUser;
import static com.walnutek.fermentationtank.config.mongo.CriteriaBuilder.where;
import com.walnutek.fermentationtank.config.mongo.UpdateBuilder;
import com.walnutek.fermentationtank.model.entity.Laboratory;
import com.walnutek.fermentationtank.model.entity.User;
import com.walnutek.fermentationtank.model.vo.Page;
import com.walnutek.fermentationtank.model.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static com.walnutek.fermentationtank.config.mongo.CriteriaBuilder.where;
import static com.walnutek.fermentationtank.model.service.Utils.hasText;

@Repository
public class LaboratoryDao extends BaseDao<Laboratory> {
    public List<Laboratory> selectByOwnerId(String ownerId) {
        return selectList(List.of(where(Laboratory::getOwnerId).is(ownerId).build()));
    }

}
