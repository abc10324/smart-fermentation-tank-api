package com.walnutek.fermentationtank.controller;

import com.walnutek.fermentationtank.config.Const;
import com.walnutek.fermentationtank.config.auth.HasRole;
import com.walnutek.fermentationtank.model.entity.User;
import com.walnutek.fermentationtank.model.service.ProjectService;
import com.walnutek.fermentationtank.model.vo.Page;
import com.walnutek.fermentationtank.model.vo.ProjectVO;
import com.walnutek.fermentationtank.model.vo.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "專案")
@RestController
@RequestMapping("/project")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Operation(summary = "取得專案清單")
    @SecurityRequirement(name = Const.BEARER_JWT)
    @Parameter(name = Const.KEYWORD, schema = @Schema(implementation = String.class), description = "專案名稱、目標裝置")
    @Parameter(name = Const.PAGE, schema = @Schema(implementation = Integer.class), description = "頁數")
    @Parameter(name = Const.LIMIT, schema = @Schema(implementation = Integer.class), description = "每頁幾筆")
    @Parameter(name = Const.SORT_FIELD_KEY, schema = @Schema(implementation = String.class), description = "排序欄位")
    @Parameter(name = Const.SORT_DIRECTION_KEY, schema = @Schema(implementation = String.class), description = "排序方向", example = "asc|desc")
    @GetMapping("/{laboratoryId}")
    public Page<ProjectVO> search(@Parameter(name = "laboratoryId", description = "實驗室ID") @PathVariable String laboratoryId,
                                  @Parameter(hidden = true) @RequestParam Map<String, Object> paramMap){
        projectService.checkUserIsBelongToLaboratory(laboratoryId, true);
        return projectService.search(laboratoryId, paramMap);
    }

    @Operation(summary = "新增專案")
    @SecurityRequirement(name = Const.BEARER_JWT)
    @HasRole({User.Role.LAB_ADMIN, User.Role.LAB_USER})
    @PostMapping("/{laboratoryId}")
    public Response createProject(@Parameter(name = "laboratoryId", description = "實驗室ID") @PathVariable String laboratoryId,
                                 @RequestBody ProjectVO vo) {
        projectService.checkUserIsLaboratoryOwner(laboratoryId, false);
        projectService.createProject(laboratoryId, vo);
        return Response.ok();
    }

    @Operation(summary = "更新專案")
    @SecurityRequirement(name = Const.BEARER_JWT)
    @HasRole({User.Role.LAB_ADMIN, User.Role.LAB_USER})
    @PutMapping("/{laboratoryId}/{projectId}")
    public Response updateProject(
            @Parameter(name = "laboratoryId", description = "實驗室ID") @PathVariable String laboratoryId,
            @Parameter(name = "projectId", description = "專案ID") @PathVariable String projectId,
            @RequestBody ProjectVO vo) {
        projectService.checkUserIsLaboratoryOwner(laboratoryId, false);
        projectService.updateProject(laboratoryId, projectId, vo);
        return Response.ok();
    }

    @Operation(summary = "刪除專案")
    @SecurityRequirement(name = Const.BEARER_JWT)
    @HasRole({User.Role.LAB_ADMIN, User.Role.LAB_USER})
    @DeleteMapping("/{laboratoryId}/{projectId}")
    public Response deleteProject(
            @Parameter(name = "laboratoryId", description = "實驗室ID") @PathVariable String laboratoryId,
            @Parameter(name = "projectId", description = "專案ID") @PathVariable String projectId
    ) {
        projectService.checkUserIsLaboratoryOwner(laboratoryId, false);
        projectService.deleteProject(laboratoryId, projectId);
        return Response.ok();
    }
}
