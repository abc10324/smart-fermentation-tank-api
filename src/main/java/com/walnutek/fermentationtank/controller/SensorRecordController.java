package com.walnutek.fermentationtank.controller;

import com.walnutek.fermentationtank.model.service.SensorRecordService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Sensor發送紀錄")
@RestController
@RequestMapping("/sensor-record")
public class SensorRecordController {

    @Autowired
    private SensorRecordService sensorRecordService;
}
