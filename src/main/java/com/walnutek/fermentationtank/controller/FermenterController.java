package com.walnutek.fermentationtank.controller;

import com.walnutek.fermentationtank.config.Const;
import com.walnutek.fermentationtank.model.service.FermenterService;
import com.walnutek.fermentationtank.model.vo.FermenterVO;
import com.walnutek.fermentationtank.model.vo.Page;
import com.walnutek.fermentationtank.model.vo.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "醱酵槽")
@RestController
@RequestMapping("/fermenter")
public class FermenterController {

    @Autowired
    private FermenterService fermenterService;

    @Operation(summary = "取得醱酵槽清單")
    @SecurityRequirement(name = Const.BEARER_JWT)
    @Parameter(name = "keyword", schema = @Schema(implementation = String.class), description = "醱酵槽名稱")
    @Parameter(name = "page", schema = @Schema(implementation = Integer.class), description = "頁數")
    @Parameter(name = "limit", schema = @Schema(implementation = Integer.class), description = "每頁幾筆")
    @Parameter(name = "orderBy", schema = @Schema(implementation = String.class), description = "排序欄位")
    @Parameter(name = "sort", schema = @Schema(implementation = String.class), description = "排序方向", example = "asc|desc")
    @GetMapping("/{laboratoryId}")
    public Page<FermenterVO> search(@Parameter(name = "laboratoryId", description = "實驗室ID") @PathVariable String laboratoryId,
                                    @Parameter(hidden = true) @RequestParam Map<String, Object> paramMap){
        return fermenterService.search(laboratoryId, paramMap);
    }

    @Operation(summary = "取得所有醱酵槽清單")
    @SecurityRequirement(name = Const.BEARER_JWT)
    @GetMapping("/{laboratoryId}/list")
    public List<FermenterVO> getFermenterOptionListByLab(
            @Parameter(name = "laboratoryId", description = "實驗室ID") @PathVariable String laboratoryId
    ){
        return fermenterService.list(laboratoryId);
    }

    @Operation(summary = "新增醱酵槽")
    @SecurityRequirement(name = Const.BEARER_JWT)
    @PostMapping("/{laboratoryId}")
    public Response createFermenter(@Parameter(name = "laboratoryId", description = "實驗室ID") @PathVariable String laboratoryId,
                                     @RequestBody FermenterVO vo) {
        fermenterService.createFermenter(laboratoryId, vo);
        return Response.ok();
    }

    @Operation(summary = "更新醱酵槽")
    @SecurityRequirement(name = Const.BEARER_JWT)
    @PutMapping("/{laboratoryId}/{fermenterId}")
    public Response updateFermenter(
            @Parameter(name = "laboratoryId", description = "實驗室ID") @PathVariable String laboratoryId,
            @Parameter(name = "fermenterId", description = "醱酵槽ID") @PathVariable String fermenterId,
            @RequestBody FermenterVO vo) {
        fermenterService.updateFermenter(laboratoryId, fermenterId, vo);
        return Response.ok();
    }

    @Operation(summary = "刪除醱酵槽")
    @SecurityRequirement(name = Const.BEARER_JWT)
    @DeleteMapping("/{laboratoryId}/{fermenterId}")
    public Response deleteFermenter(
            @Parameter(name = "laboratoryId", description = "實驗室ID") @PathVariable String laboratoryId,
            @Parameter(name = "fermenterId", description = "醱酵槽ID") @PathVariable String fermenterId
    ) {
        fermenterService.deleteFermenter(laboratoryId, fermenterId);
        return Response.ok();
    }
}
