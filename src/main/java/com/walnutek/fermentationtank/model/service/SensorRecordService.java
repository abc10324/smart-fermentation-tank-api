package com.walnutek.fermentationtank.model.service;

import com.walnutek.fermentationtank.model.dao.SensorRecordDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SensorRecordService {

    @Autowired
    private SensorRecordDao sensorRecordDao;
}
