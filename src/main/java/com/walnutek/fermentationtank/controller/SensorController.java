package com.walnutek.fermentationtank.controller;

import com.walnutek.fermentationtank.config.Const;
import com.walnutek.fermentationtank.model.entity.BaseColumns;
import com.walnutek.fermentationtank.model.entity.Sensor;
import com.walnutek.fermentationtank.model.entity.SensorRecord;
import com.walnutek.fermentationtank.model.service.AlertRecordService;
import com.walnutek.fermentationtank.model.service.AlertService;
import com.walnutek.fermentationtank.model.service.SensorRecordService;
import com.walnutek.fermentationtank.model.service.SensorService;
import com.walnutek.fermentationtank.model.vo.Response;
import com.walnutek.fermentationtank.model.vo.SensorVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Tag(name = "Sensor")
@RestController
@RequestMapping("/sensor")
public class SensorController {

    @Autowired
    private SensorService sensorService;

    @Autowired
    private SensorRecordService sensorRecordService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private AlertRecordService alertRecordService;

    @Operation(summary = "取得所有Sensor紀錄清單")
    @SecurityRequirement(name = Const.BEARER_JWT)
    @GetMapping("/{laboratoryId}/{deviceId}/list")
    public List<SensorRecord> getSensorRecordListByDeviceId(
            @Parameter(name = "laboratoryId", description = "實驗室ID") @PathVariable String laboratoryId,
            @Parameter(name = "deviceId", description = "裝置ID") @PathVariable String deviceId
    ){
        var sensorList = getSensorListByLaboratoryIdAndDeviceId(laboratoryId, deviceId);
        var sensorIdList = sensorList.stream().map(BaseColumns::getId).toList();
        return getSensorRecordListBySensorIdList(sensorIdList);
    }

    @Operation(summary = "取得目標欄位清單")
    @SecurityRequirement(name = Const.BEARER_JWT)
    @GetMapping("/{laboratoryId}/{deviceId}/checkField-list")
    public List<String> getCheckFieldListByDeviceId(
            @Parameter(name = "laboratoryId", description = "實驗室ID") @PathVariable String laboratoryId,
            @Parameter(name = "deviceId", description = "裝置ID") @PathVariable String deviceId
    ){
        var sensorList = getSensorListByLaboratoryIdAndDeviceId(laboratoryId, deviceId);
        var checkFieldList = new ArrayList<String>();
        sensorList.forEach(sensor -> checkFieldList.addAll(sensor.getCheckFieldList()));
        return checkFieldList;
    }

    private List<Sensor> getSensorListByLaboratoryIdAndDeviceId(String laboratoryId, String deviceId) {
        var sensorParamMap = new HashMap<String, Object>();
        sensorParamMap.put("laboratoryId", laboratoryId);
        sensorParamMap.put("deviceId", deviceId);
        var sensorList = sensorService.findListByQuery(sensorParamMap);
        return sensorList;
    }

    private List<SensorRecord> getSensorRecordListBySensorIdList(List<String> sensorIdList) {
        var sensorRecordParamMap = new HashMap<String, Object>();
        sensorRecordParamMap.put("sensorIdList", sensorIdList);
        var sensorRecordList = sensorRecordService.findListByQuery(sensorRecordParamMap);
        return sensorRecordList;
    }

    @Operation(summary = "新增Sensor")
    @SecurityRequirement(name = Const.BEARER_JWT)
    @PostMapping("/{laboratoryId}")
    public Response createSensor(@Parameter(name = "laboratoryId", description = "實驗室ID") @PathVariable String laboratoryId,
                                  @RequestBody SensorVO vo) {
        sensorService.createSensorAndRecord(laboratoryId, vo);
        //檢查目標欄位的條件跟閥值並發布警報
        var alertParamMap = new HashMap<String, Object>();
        alertParamMap.put("laboratoryId", laboratoryId);
        alertParamMap.put("deviceId", vo.getDeviceId());
        alertService.checkSensorUploadDataAndSendAlertRecord(alertParamMap, vo.getUploadData());

        return Response.ok();
    }


}
