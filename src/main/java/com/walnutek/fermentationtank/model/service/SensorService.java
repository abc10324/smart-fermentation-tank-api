package com.walnutek.fermentationtank.model.service;

import com.walnutek.fermentationtank.config.mongo.CriteriaBuilder;
import com.walnutek.fermentationtank.exception.AppException;
import com.walnutek.fermentationtank.model.dao.LaboratoryDao;
import com.walnutek.fermentationtank.model.dao.SensorDao;
import com.walnutek.fermentationtank.model.dao.SensorRecordDao;
import com.walnutek.fermentationtank.model.entity.BaseColumns;
import com.walnutek.fermentationtank.model.entity.Sensor;
import com.walnutek.fermentationtank.model.entity.SensorRecord;
import com.walnutek.fermentationtank.model.vo.SensorRecordVO;
import com.walnutek.fermentationtank.model.vo.SensorVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
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

    public List<SensorRecord> createSensorAndRecord(String laboratoryId, SensorVO sensorVO){
        isLabAvailable(laboratoryId);
        checkCreateOrUpdateField(sensorVO);
        var resulList = new ArrayList<SensorRecord>();
        var sensor = isSenorExists(sensorVO);
        String sensorId;
        if(Objects.isNull(sensor)){
            // 寫入sensor資料
            var insertOne = sensorVO.toSensor(new Sensor());
            sensorDao.insert(insertOne);
            sensorId = insertOne.getId();
        }else {
            sensorId = sensor.getId();
        }
        var uploadList = sensorVO.getUploadList();
        if(!uploadList.isEmpty()){
            uploadList.forEach(recordVO ->{
                recordVO.setSensorId(sensorId);
                var sensorRecord = isSenorRecordExists(recordVO);
                if(Objects.isNull(sensorRecord)){
                    // 寫入sensorRecord資料
                    var insertOne = recordVO.toSensorRecord(new SensorRecord());
                    sensorRecordDao.insert(insertOne);
                    resulList.add(insertOne);
                }
            });
        }
        return resulList;
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

    private Sensor isSenorExists(SensorVO vo) {
        var sensorQuery = List.of(
                where(Sensor::getLaboratoryId).is(vo.getLaboratoryId()).build(),
                where(Sensor::getDeviceId).is(vo.getDeviceId()).build(),
                where(Sensor::getLabel).is(vo.getLabel()).build()
        );
        return sensorDao.selectOne(sensorQuery);
    }

    private SensorRecord isSenorRecordExists(SensorRecordVO vo) {
        var sensorRecordQuery = List.of(
                where(SensorRecord::getSensorId).is(vo.getSensorId()).build(),
                where(SensorRecord::getRecordTime).is(vo.getRecordTime()).build()
        );
        return sensorRecordDao.selectOne(sensorRecordQuery);
    }

    private void checkCreateOrUpdateField(SensorVO vo){
        if(!StringUtils.hasText(vo.getLaboratoryId())
                || !StringUtils.hasText(vo.getDeviceId())
                || !StringUtils.hasText(vo.getLabel())
                || vo.getUploadList().isEmpty()
        ) throw new AppException(AppException.Code.E002, "必上傳欄位資料不正確");
    }
}
