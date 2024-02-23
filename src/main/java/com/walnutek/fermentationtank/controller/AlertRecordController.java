package com.walnutek.fermentationtank.controller;

import com.walnutek.fermentationtank.config.Const;
import com.walnutek.fermentationtank.model.entity.AlertRecord.AlertState;
import com.walnutek.fermentationtank.model.service.AlertRecordService;
import com.walnutek.fermentationtank.model.vo.AlertRecordVO;
import com.walnutek.fermentationtank.model.vo.AlertVO;
import com.walnutek.fermentationtank.model.vo.Page;
import com.walnutek.fermentationtank.model.vo.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "警報紀錄")
@RestController
@RequestMapping("/alert-record")
public class AlertRecordController {
    @Autowired
    private AlertRecordService alertRecordService;

    @Operation(summary = "取得警報紀錄清單")
    @SecurityRequirement(name = Const.BEARER_JWT)
    @Parameter(name = Const.KEYWORD, schema = @Schema(implementation = String.class), description = "警報名稱、裝置分類、目標裝置、目標欄位")
    @Parameter(name = "alertState", schema = @Schema(implementation = AlertState.class), description = "警報狀態")
    @Parameter(name = Const.PAGE, schema = @Schema(implementation = Integer.class), description = "頁數")
    @Parameter(name = Const.LIMIT, schema = @Schema(implementation = Integer.class), description = "每頁幾筆")
    @Parameter(name = Const.SORT_FIELD_KEY, schema = @Schema(implementation = String.class), description = "排序欄位")
    @Parameter(name = Const.SORT_DIRECTION_KEY, schema = @Schema(implementation = String.class), description = "排序方向", example = "asc|desc")
    @GetMapping("/{laboratoryId}")
    public Page<AlertRecordVO> search(
            @Parameter(name = "laboratoryId", description = "實驗室ID") @PathVariable String laboratoryId,
            @Parameter(hidden = true) @RequestParam Map<String, Object> paramMap
    ){
        return alertRecordService.search(laboratoryId, paramMap);
    }

    @Operation(summary = "解除警報")
    @SecurityRequirement(name = Const.BEARER_JWT)
    @PutMapping("/{laboratoryId}/{alertRecordId}")
    public Response updateAlertRecord(
            @Parameter(name = "laboratoryId", description = "實驗室ID") @PathVariable String laboratoryId,
            @Parameter(name = "alertRecordId", description = "警報紀錄ID") @PathVariable String alertRecordId,
            @RequestBody AlertRecordVO vo) {
        alertRecordService.updateAlertRecord(laboratoryId, alertRecordId, vo);
        return Response.ok();
    }
}
