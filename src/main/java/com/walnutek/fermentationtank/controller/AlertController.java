package com.walnutek.fermentationtank.controller;

import com.walnutek.fermentationtank.config.Const;
import com.walnutek.fermentationtank.model.service.AlertService;
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

@Tag(name = "警報設定")
@RestController
@RequestMapping("/alert")
public class AlertController {

    @Autowired
    private AlertService alertService;

    @Operation(summary = "取得警報設定清單")
    @SecurityRequirement(name = Const.BEARER_JWT)
    @Parameter(name = Const.KEYWORD, schema = @Schema(implementation = String.class), description = "警報名稱、裝置分類、目標裝置、目標欄位")
    @Parameter(name = Const.PAGE, schema = @Schema(implementation = Integer.class), description = "頁數")
    @Parameter(name = Const.LIMIT, schema = @Schema(implementation = Integer.class), description = "每頁幾筆")
    @Parameter(name = Const.SORT_FIELD_KEY, schema = @Schema(implementation = String.class), description = "排序欄位")
    @Parameter(name = Const.SORT_DIRECTION_KEY, schema = @Schema(implementation = String.class), description = "排序方向", example = "asc|desc")
    @GetMapping("/{laboratoryId}")
    public Page<AlertVO> search(@Parameter(name = "laboratoryId", description = "實驗室ID") @PathVariable String laboratoryId,
                                @Parameter(hidden = true) @RequestParam Map<String, Object> paramMap){
        alertService.checkUserIsBelongToLaboratory(laboratoryId, true);
        return alertService.search(laboratoryId, paramMap);
    }

    @Operation(summary = "新增警報")
    @SecurityRequirement(name = Const.BEARER_JWT)
    @PostMapping("/{laboratoryId}")
    public Response createAlert(@Parameter(name = "laboratoryId", description = "實驗室ID") @PathVariable String laboratoryId,
                                @RequestBody AlertVO vo) {
        alertService.checkUserIsBelongToLaboratory(laboratoryId, true);
        alertService.createAlert(laboratoryId, vo);
        return Response.ok();
    }

    @Operation(summary = "更新警報設定")
    @SecurityRequirement(name = Const.BEARER_JWT)
    @PutMapping("/{laboratoryId}/{alertId}")
    public Response updateAlert(
            @Parameter(name = "laboratoryId", description = "實驗室ID") @PathVariable String laboratoryId,
            @Parameter(name = "alertId", description = "警報設定ID") @PathVariable String alertId,
            @RequestBody AlertVO vo) {
        alertService.checkUserIsBelongToLaboratory(laboratoryId, true);
        alertService.updateAlert(alertId, vo);
        return Response.ok();
    }

    @Operation(summary = "刪除警報設定")
    @SecurityRequirement(name = Const.BEARER_JWT)
    @DeleteMapping("/{laboratoryId}/{alertId}")
    public Response deleteAlert(
            @Parameter(name = "laboratoryId", description = "實驗室ID") @PathVariable String laboratoryId,
            @Parameter(name = "alertId", description = "警報設定ID") @PathVariable String alertId
    ) {
        alertService.checkUserIsBelongToLaboratory(laboratoryId, true);
        alertService.deleteAlert(alertId);
        return Response.ok();
    }
}
