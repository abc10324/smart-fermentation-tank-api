package com.walnutek.fermentationtank.model.dao;

import com.walnutek.fermentationtank.model.entity.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ProjectDao extends BaseDao<Project> {

    @Autowired
    private MongoTemplate template;

}
