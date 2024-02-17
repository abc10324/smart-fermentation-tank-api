package com.walnutek.fermentationtank.controller;

import com.walnutek.fermentationtank.config.Const;
import com.walnutek.fermentationtank.model.entity.AlertRecord;
import com.walnutek.fermentationtank.model.entity.BaseColumns;
import com.walnutek.fermentationtank.model.service.*;
import com.walnutek.fermentationtank.model.vo.DashboardVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Dashboard")
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private FermenterService fermenterService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    @Autowired
    private LineNotifyService lineNotifyService;

    @Autowired
    private AlertRecordService alertRecordService;

    @Operation(summary = "dashboard資料")
    @SecurityRequirement(name = Const.BEARER_JWT)
    @GetMapping("/{laboratoryId}")
    public DashboardVO getDashboard(
            @Parameter(name = "laboratoryId", description = "實驗室ID") @PathVariable String laboratoryId
    ){
        var vo = new DashboardVO();

        var fermenterNum = fermenterService.countFermenterNum(laboratoryId);
        vo.setFermenterNum(fermenterNum);

        var projectNum = projectService.countProjectNum(laboratoryId);
        vo.setProjectNum(projectNum);

        var userParamMap = new HashMap<String, Object>();
        userParamMap.put("labList", List.of(laboratoryId));
        userParamMap.put("status", BaseColumns.Status.ACTIVE);
        var userNum = userService.countUserNum(userParamMap);
        vo.setUserNum(userNum);

        var lineNotifyNum = lineNotifyService.countLineNotifyNum(laboratoryId);
        vo.setLineNotifyNum(lineNotifyNum);

        var alertParamMap = new HashMap<String, Object>();
        alertParamMap.put("state", AlertRecord.AlertState.ISSUE);
        var alertNum = alertRecordService.countAlertRecordNum(laboratoryId, alertParamMap);
        vo.setAlertNum(alertNum);

        return vo;
    }
}
