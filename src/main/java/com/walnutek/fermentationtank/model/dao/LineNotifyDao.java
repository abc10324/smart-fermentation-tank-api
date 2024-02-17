package com.walnutek.fermentationtank.model.dao;

import com.walnutek.fermentationtank.model.entity.LineNotify;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class LineNotifyDao extends BaseDao<LineNotify> {

    @Autowired
    private MongoTemplate template;
}
