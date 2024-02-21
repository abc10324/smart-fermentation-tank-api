package com.walnutek.fermentationtank.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@Schema(title = "DashboardVO")
@Data
@EqualsAndHashCode(callSuper = false)
public class DashboardVO {

    @Schema(title = "醱酵槽總數量")
    private Integer fermenterNum;

    @Schema(title = "醱酵槽列表")
//    private Map<String,List<FermenterVO>> fermenterList;
    private List<DashboardDataVO>  fermenterList;

    @Schema(title = "總專案筆數")
    private Integer projectNum;

    @Schema(title = "專案列表")
//    private Map<String,List<ProjectVO>> projectList;
    private List<DashboardDataVO>  projectList;

    @Schema(title = "總帳號數量")
    private Integer userNum;

    @Schema(title = "帳號列表")
//    private Map<String,List<UserVO>> userList;
    private List<DashboardDataVO>  userList;

    @Schema(title = "Line Notify註冊數量")
    private Integer lineNotifyNum;

    @Schema(title = "Line Notify列表")
//    private Map<String,List<LineNotifyVO>> lineNotifyList;
    private List<DashboardDataVO> lineNotifyList;

    @Schema(title = "未解決的警報數量")
    private Integer alertNum;

    @Schema(title = "警報列表")
//    private Map<String,List<AlertRecordVO>> alertList;
    private List<DashboardDataVO> alertList;

}


