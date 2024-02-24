package com.walnutek.fermentationtank.model.service;

import com.walnutek.fermentationtank.config.mongo.CriteriaBuilder;
import com.walnutek.fermentationtank.model.dao.SensorRecordDao;
import com.walnutek.fermentationtank.model.entity.SensorRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static com.walnutek.fermentationtank.config.mongo.CriteriaBuilder.where;
import static com.walnutek.fermentationtank.model.service.Utils.hasArray;
import static com.walnutek.fermentationtank.model.service.Utils.hasText;

@Service
@Transactional
public class SensorRecordService {

    @Autowired
    private SensorRecordDao sensorRecordDao;

    public List<SensorRecord> findListByQuery(Map<String, Object> paramMap){
        var sensorRecordQuery = Stream.of(
                        where(hasText(paramMap.get("sensorId")), SensorRecord::getSensorId).is(paramMap.get("sensorId")),
                        where(hasArray(paramMap.get("sensorIdList")), SensorRecord::getSensorId).in(paramMap.get("sensorIdList"))
                ).map(CriteriaBuilder::build)
                .filter(Objects::nonNull)
                .toList();
        return sensorRecordDao.selectList(sensorRecordQuery);
    }
}
