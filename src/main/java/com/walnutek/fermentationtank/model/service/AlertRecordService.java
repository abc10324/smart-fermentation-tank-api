package com.walnutek.fermentationtank.model.service;

import com.walnutek.fermentationtank.config.mongo.CriteriaBuilder;
import com.walnutek.fermentationtank.model.dao.AlertDao;
import com.walnutek.fermentationtank.model.dao.AlertRecordDao;
import com.walnutek.fermentationtank.model.entity.Alert;
import com.walnutek.fermentationtank.model.entity.AlertRecord;
import com.walnutek.fermentationtank.model.entity.BaseColumns;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static com.walnutek.fermentationtank.config.mongo.CriteriaBuilder.where;
import static com.walnutek.fermentationtank.model.service.Utils.hasText;

@Service
@Transactional
public class AlertRecordService {

    @Autowired
    private AlertDao alertDao;

    @Autowired
    private AlertRecordDao alertRecordDao;

    public Integer countAlertRecordNum(String laboratoryId, Map<String, Object> paramMap){
        var alertQuery = Stream.of(
                        where(Alert::getLaboratoryId).is(laboratoryId),
                        where(Alert::getStatus).is(BaseColumns.Status.ACTIVE),
                        where(hasText(paramMap.get("keyword")), Alert::getName).like(paramMap.get("keyword"))
                                .or(where(Alert::getCheckField).like(paramMap.get("keyword")))
                ).map(CriteriaBuilder::build)
                .filter(Objects::nonNull)
                .toList();
        var alertIdList = alertDao.selectList(alertQuery).stream().map(BaseColumns::getId).toList();
        var alertRecordQuery = Stream.of(
                        where(AlertRecord::getAlertId).in(alertIdList),
                        where(hasText(paramMap.get("state")), AlertRecord::getState).is(paramMap.get("state"))
                ).map(CriteriaBuilder::build)
                .filter(Objects::nonNull)
                .toList();
        return Math.toIntExact(alertRecordDao.count(alertRecordQuery));
    }
}
