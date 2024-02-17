package com.walnutek.fermentationtank.model.service;

import com.walnutek.fermentationtank.model.dao.LineNotifyDao;
import com.walnutek.fermentationtank.model.entity.BaseColumns;
import com.walnutek.fermentationtank.model.entity.LineNotify;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.walnutek.fermentationtank.config.mongo.CriteriaBuilder.where;

@Service
@Transactional
public class LineNotifyService {

    @Autowired
    private LineNotifyDao lineNotifyDao;

    public Integer countLineNotifyNum(String laboratoryId){
        var query = List.of(
                where(LineNotify::getLaboratoryId).is(laboratoryId).build(),
                where(LineNotify::getStatus).is(BaseColumns.Status.ACTIVE).build());
        return Math.toIntExact(lineNotifyDao.count(query));
    }

}
