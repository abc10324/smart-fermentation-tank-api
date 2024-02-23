package com.walnutek.fermentationtank.model.service;

import com.walnutek.fermentationtank.model.dao.SensorDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SensorService {

    @Autowired
    private SensorDao sensorDao;
}
