package com.walnutek.fermentationtank.controller;

import com.walnutek.fermentationtank.config.Const;
import com.walnutek.fermentationtank.model.entity.AlertRecord;
import com.walnutek.fermentationtank.model.entity.BaseColumns;
import com.walnutek.fermentationtank.model.entity.Fermenter;
import com.walnutek.fermentationtank.model.entity.Laboratory;
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
import java.util.stream.Collectors;

import static com.walnutek.fermentationtank.config.mongo.CriteriaBuilder.where;

@Tag(name = "Dashboard")
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private LaboratoryService laboratoryService;

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
    @GetMapping()
    public DashboardVO getUserDashboard(){
        var userLabList = userService.getOwnLabList();
        var userLabIdList = userLabList.stream().map(BaseColumns::getId).toList();
        var userLabMap = userLabList.stream().collect(Collectors.toMap(Laboratory::getId, Laboratory::getName));

        var vo = new DashboardVO();

        // 醱酵槽列表
        var fermenterList = fermenterService.listAllGroupByLaboratoryId(userLabIdList, userLabMap);
        vo.setFermenterList(fermenterList);

        // 專案列表
        var fermenterLabMap = getUserFermenterMap(userLabIdList);
        var projectList = projectService.listAllGroupByLaboratoryId(userLabIdList, userLabMap, fermenterLabMap);
        vo.setProjectList(projectList);

        var userParamMap = new HashMap<String, Object>();
        userParamMap.put("labList", userLabList);
        userParamMap.put("status", BaseColumns.Status.ACTIVE);
        // 帳號列表
        var userList = userService.listAllGroupByLaboratoryId(userLabIdList, userLabMap);
        vo.setUserList(userList);

        // Line Notify列表
        var lineNotifyList = lineNotifyService.listAllGroupByLaboratoryId(userLabIdList, userLabMap);
        vo.setLineNotifyList(lineNotifyList);

        var alertParamMap = new HashMap<String, Object>();
        alertParamMap.put("state", AlertRecord.AlertState.ISSUE);
        // 警報列表
        var alertList = alertRecordService.listAllGroupByLaboratoryId(userLabIdList, userLabMap, alertParamMap);
        vo.setAlertList(alertList);

        return vo;
    }

    private Map<String, String> getUserFermenterMap(List<String> userLabList){
        var query = List.of(
                where(Fermenter::getLaboratoryId).in(userLabList).build(),
                where(Fermenter::getStatus).is(BaseColumns.Status.ACTIVE).build()
        );
        var fermenterLabMap = new HashMap<String,String>();
        fermenterService.listByQuery(query).stream().map(fermenter -> fermenterLabMap.put(fermenter.getId(), fermenter.getName()));
        return fermenterLabMap;
    }

    /* Dashboard 資料 ver.77
    @Operation(summary = "dashboard資料2")
    @SecurityRequirement(name = Const.BEARER_JWT)
    @GetMapping()
    public DashboardVO2 getUserDashboard2() {
        var vo = new DashboardVO2();

        userService.getOwnLabList() // 依照帳號類別取得擁有的實驗室清單
            .forEach(lab -> {
                Optional.ofNullable(fermenterService.list(lab.getId()))
                        .filter(resultList -> !resultList.isEmpty())
                        .ifPresent(dataList -> vo.fermenter.groupDataList
                                .add(DashboardVO2.StaticInfo.Item.of(lab.getId(), lab.getName(), dataList)));

                Optional.ofNullable(projectService.list(lab.getId()))
                        .filter(resultList -> !resultList.isEmpty())
                        .ifPresent(dataList -> vo.project.groupDataList
                                .add(DashboardVO2.StaticInfo

                Optional.ofNullable(userService.list(lab.getId()))
                        .filter(resultList -> !resultList.isEmpty())
                        .ifPresent(dataList -> vo.user.groupDataList
                                .add(DashboardVO2.StaticInfo.Item.of(lab.getId(), lab.getName(), dataList)));

                Optional.ofNullable(lineNotifyService.list(lab.getId()))
                        .filter(resultList -> !resultList.isEmpty())
                        .ifPresent(dataList -> vo.lineNotify.groupDataList
                                .add(DashboardVO2.StaticInfo.Item.of(lab.getId(), lab.getName(), dataList)));

                Optional.ofNullable(alertRecordService.listUnfinished(lab.getId()))
                        .filter(resultList -> !resultList.isEmpty())
                        .ifPresent(dataList -> vo.alert.groupDataList
                                .add(DashboardVO2.StaticInfo.Item.of(lab.getId(), lab.getName(), dataList)));

            });

        return vo;
    }
    @Schema(title = "DashboardVO2")
    @Data
    private static class DashboardVO2 {

        @Schema(title = "醱酵槽統計資料")
        private StaticInfo<FermenterVO> fermenter = new StaticInfo<>();

        @Schema(title = "專案統計資料")
        private StaticInfo<ProjectVO> project = new StaticInfo<>();

        @Schema(title = "帳號統計資料")
        private StaticInfo<UserVO> user = new StaticInfo<>();

        @Schema(title = "LineNotify統計資料")
        private StaticInfo<LineNotifyVO> lineNotify = new StaticInfo<>();

        @Schema(title = "未解決的警報統計資料")
        private StaticInfo<AlertRecordVO> alert = new StaticInfo<>();

        @Schema(title = "統計資料")
        @Data
        private static class StaticInfo<T> {

            @Schema(title = "群組資料清單")
            private List<Item<T>> groupDataList = new ArrayList<>();

            @Schema(title = "群組資料統計數量")
            public Integer getTotal() {
                return Optional.ofNullable(groupDataList)
                        .orElse(List.of())
                        .stream()
                        .map(Item::getTotal)
                        .reduce(Integer::sum)
                        .orElse(0);
            }

            private static class Item<T> {

                @Schema(title = "實驗室ID")
                private String labId;

                @Schema(title = "實驗室名稱")
                private String labName;

                @Schema(title = "資料清單")
                private List<T> dataList = new ArrayList<>();;

                @Schema(title = "資料總數")
                public Integer getTotal() {
                    return Optional.ofNullable(dataList)
                            .map(List::size)
                            .orElse(0);
                }

                public static <T> Item<T> of(String labId, String labName, List<T> dataList) {
                    var vo = new Item<T>();
                    vo.labId = labId;
                    vo.labName = labName;
                    vo.dataList = dataList;

                    return vo;
                }

            }
        }
    }*/
}
