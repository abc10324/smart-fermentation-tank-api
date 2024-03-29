package com.walnutek.fermentationtank.model.service;

import com.walnutek.fermentationtank.config.mongo.CriteriaBuilder;
import com.walnutek.fermentationtank.model.dao.SensorRecordDao;
import com.walnutek.fermentationtank.model.entity.SensorRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.walnutek.fermentationtank.config.mongo.CriteriaBuilder.where;
import static com.walnutek.fermentationtank.model.service.Utils.*;

@Service
@Transactional
public class SensorRecordService {

    @Autowired
    private SensorRecordDao sensorRecordDao;

    public List<SensorRecord> findListByQuery(Map<String, Object> paramMap){
        var queryList = new ArrayList<CriteriaBuilder>();
        if(paramMap.get("startTime") != null && paramMap.get("endTime") != null ){
            if (paramMap.get("startTime") instanceof LocalDateTime startTime
                    && paramMap.get("endTime") instanceof LocalDateTime endTime) {
                var startTimeLong = ZonedDateTime.of(startTime, ZoneId.systemDefault()).toInstant().toEpochMilli();
                var endTimeLong = ZonedDateTime.of(endTime, ZoneId.systemDefault()).toInstant().toEpochMilli();
                queryList.add(where(SensorRecord::getRecordTime).gte(startTimeLong).lte(endTimeLong));
            }
        }
        if(hasText(paramMap.get("sensorId"))){
            queryList.add(where(SensorRecord::getSensorId).is(paramMap.get("sensorId")));
        }
        if(hasArray(paramMap.get("sensorIdList"))){
            queryList.add(where(SensorRecord::getSensorId).in(paramMap.get("sensorIdList")));
        }
        var sensorRecordQuery = queryList.stream().map(CriteriaBuilder::build).filter(Objects::nonNull).toList();

        var query = new Query();
        sensorRecordQuery.forEach(query::addCriteria);
        query.with(Sort.by(Sort.Direction.DESC, field(SensorRecord::getRecordTime)));
        return sensorRecordDao.selectList(query);
    }
}
