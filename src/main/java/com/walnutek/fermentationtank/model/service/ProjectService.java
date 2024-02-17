package com.walnutek.fermentationtank.model.service;

import com.walnutek.fermentationtank.model.dao.ProjectDao;
import com.walnutek.fermentationtank.model.entity.BaseColumns;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.walnutek.fermentationtank.model.entity.Project;

import java.util.List;

import static com.walnutek.fermentationtank.config.mongo.CriteriaBuilder.where;

@Service
@Transactional
public class ProjectService extends BaseService {

    @Autowired
    private ProjectDao projectDao;

    public Integer countProjectNum(String laboratoryId){
        var query = List.of(
                where(Project::getLaboratoryId).is(laboratoryId).build(),
                where(Project::getStatus).is(BaseColumns.Status.ACTIVE).build());
        return Math.toIntExact(projectDao.count(query));
    }
}
