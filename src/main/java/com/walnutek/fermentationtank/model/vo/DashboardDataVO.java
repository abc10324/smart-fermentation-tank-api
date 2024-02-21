package com.walnutek.fermentationtank.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class DashboardDataVO<T> {

    @Schema(title = "實驗室名稱")
    public String laboratory;

    @Schema(title = "實驗室Id")
    public String laboratoryId;

    @Schema(title = "資料數量")
    public Integer total;

    @Schema(title = "資料列表")
    public List<T> data;

}
