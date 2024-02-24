package com.walnutek.fermentationtank.model.service;

import com.walnutek.fermentationtank.config.mongo.CriteriaBuilder;
import com.walnutek.fermentationtank.exception.AppException;
import com.walnutek.fermentationtank.model.dao.LaboratoryDao;
import com.walnutek.fermentationtank.model.dao.SensorDao;
import com.walnutek.fermentationtank.model.dao.SensorRecordDao;
import com.walnutek.fermentationtank.model.entity.AlertRecord;
import com.walnutek.fermentationtank.model.entity.BaseColumns;
import com.walnutek.fermentationtank.model.entity.Sensor;
import com.walnutek.fermentationtank.model.entity.SensorRecord;
import com.walnutek.fermentationtank.model.vo.SensorVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static com.walnutek.fermentationtank.config.mongo.CriteriaBuilder.where;
import static com.walnutek.fermentationtank.model.service.Utils.hasText;

@Service
@Transactional
public class SensorService {

    @Autowired
    private SensorDao sensorDao;

    @Autowired
    private SensorRecordDao sensorRecordDao;

    @Autowired
    private LaboratoryDao laboratoryDao;

    public String createSensorAndRecord(String laboratoryId, SensorVO vo){
        isLabAvailable(laboratoryId);
        checkCreateOrUpdateField(vo);
        var sensor = isSenorExists(vo);
        String sensorId;
        if(Objects.isNull(sensor)){
            // 寫入sensor資料
            var insertOne = vo.toSensor(new Sensor());
            sensorDao.insert(insertOne);
            sensorId = insertOne.getId();
        }else {
            sensorId = sensor.getId();
        }
        vo.setId(sensorId);
        // 寫入sensorRecord資料
        var sensorRecord = vo.toSensorRecord(new SensorRecord());
        sensorRecordDao.insert(sensorRecord);
        return sensorRecord.getId();
    }

    public List<Sensor> findListByQuery(Map<String, Object> paramMap){
        var sensorQuery = Stream.of(
                where(hasText(paramMap.get("laboratoryId")), Sensor::getLaboratoryId).is(paramMap.get("laboratoryId")),
                where(hasText(paramMap.get("deviceId")), Sensor::getDeviceId).is(paramMap.get("deviceId"))
                ).map(CriteriaBuilder::build)
                .filter(Objects::nonNull)
                .toList();
        return sensorDao.selectList(sensorQuery);
    }

    private void isLabAvailable(String laboratoryId) {
        if(Objects.isNull(
                laboratoryDao.selectByIdAndStatus(laboratoryId, BaseColumns.Status.ACTIVE)
        )){
            throw new AppException(AppException.Code.E002, "無法上傳至不存在的實驗室");
        }
    }

    private Sensor isSenorExists(SensorVO sensor) {
        var sensorQuery = List.of(
                where(Sensor::getLaboratoryId).is(sensor.getLaboratoryId()).build(),
                where(Sensor::getDeviceId).is(sensor.getDeviceId()).build(),
                where(Sensor::getLabel).is(sensor.getLabel()).build()
        );
        return sensorDao.selectOne(sensorQuery);
    }

    private void checkCreateOrUpdateField(SensorVO vo){
        if(!StringUtils.hasText(vo.getLaboratoryId())
                || !StringUtils.hasText(vo.getDeviceId())
                || !StringUtils.hasText(vo.getLabel())
                || Objects.isNull(vo.getUploadData())
        ) throw new AppException(AppException.Code.E002, "必上傳欄位資料不正確");
    }
}
