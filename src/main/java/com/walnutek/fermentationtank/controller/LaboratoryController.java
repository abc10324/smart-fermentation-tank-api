package com.walnutek.fermentationtank.controller;

import com.walnutek.fermentationtank.config.Const;
import com.walnutek.fermentationtank.model.service.LaboratoryService;
import com.walnutek.fermentationtank.model.vo.LaboratoryVO;
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

@Tag(name = "實驗室")
@RestController
@RequestMapping("/laboratory")
public class LaboratoryController {

    @Autowired
    private LaboratoryService laboratoryService;

    @Operation(summary = "取得實驗室清單")
    @SecurityRequirement(name = Const.BEARER_JWT)
    @Parameter(name = Const.KEYWORD, schema = @Schema(implementation = String.class), description = "實驗室名稱")
    @Parameter(name = Const.PAGE, schema = @Schema(implementation = Integer.class), description = "頁數")
    @Parameter(name = Const.LIMIT, schema = @Schema(implementation = Integer.class), description = "每頁幾筆")
    @Parameter(name = Const.SORT_FIELD_KEY, schema = @Schema(implementation = String.class), description = "排序欄位")
    @Parameter(name = Const.SORT_DIRECTION_KEY, schema = @Schema(implementation = String.class), description = "排序方向", example = "asc|desc")
    @GetMapping
    public Page<LaboratoryVO> search(@Parameter(hidden = true) @RequestParam Map<String, Object> paramMap){
        return laboratoryService.search(paramMap);
    }

    @Operation(summary = "新增實驗室")
    @SecurityRequirement(name = Const.BEARER_JWT)
    @PostMapping
    public Response createLaboratory(@RequestBody LaboratoryVO vo) {
        laboratoryService.createLaboratory(vo);
        return Response.ok();
    }

    @Operation(summary = "更新實驗室")
    @SecurityRequirement(name = Const.BEARER_JWT)
    @PutMapping("/{laboratoryId}")
    public Response updateLaboratory(
            @Parameter(name = "laboratoryId", description = "實驗室ID")
            @PathVariable String laboratoryId,
            @RequestBody LaboratoryVO vo) {
        laboratoryService.updateLaboratory(laboratoryId, vo);
        return Response.ok();
    }

    @Operation(summary = "刪除實驗室")
    @SecurityRequirement(name = Const.BEARER_JWT)
    @DeleteMapping("/{laboratoryId}")
    public Response deleteLaboratory(
            @Parameter(name = "laboratoryId", description = "實驗室ID")
            @PathVariable String laboratoryId) {
        laboratoryService.deleteLaboratory(laboratoryId);
        return Response.ok();
    }
}
