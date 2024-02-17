package com.walnutek.fermentationtank.model.dao;

import com.walnutek.fermentationtank.model.entity.Alert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AlertDao extends BaseDao<Alert> {

    @Autowired
    private MongoTemplate template;
}
