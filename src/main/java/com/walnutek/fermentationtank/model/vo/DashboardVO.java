package com.walnutek.fermentationtank.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(title = "DashboardVO")
@Data
@EqualsAndHashCode(callSuper = false)
public class DashboardVO {

    @Schema(title = "醱酵槽數量")
    private Integer fermenterNum;

    @Schema(title = "總專案筆數")
    private Integer projectNum;

    @Schema(title = "帳號數量")
    private Integer userNum;

    @Schema(title = "Line Notify註冊數量")
    private Integer lineNotifyNum;

    @Schema(title = "未解決的警報數量")
    private Integer alertNum;

}
