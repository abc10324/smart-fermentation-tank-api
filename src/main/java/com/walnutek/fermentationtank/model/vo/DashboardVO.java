package com.walnutek.fermentationtank.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Schema(title = "DashboardVO")
@Data
@EqualsAndHashCode(callSuper = false)
public class DashboardVO {

    @Schema(title = "裝置列表")
    private List<DashboardDataVO>  deviceList;

    @Schema(title = "專案列表")
    private List<DashboardDataVO>  projectList;

    @Schema(title = "帳號列表")
    private List<DashboardDataVO>  userList;

    @Schema(title = "Line Notify列表")
    private List<DashboardDataVO> lineNotifyList;

    @Schema(title = "警報列表")
    private List<DashboardDataVO> alertList;
}


