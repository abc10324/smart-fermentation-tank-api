package com.walnutek.fermentationtank.controller;

import com.walnutek.fermentationtank.config.Const;
import com.walnutek.fermentationtank.config.auth.HasRole;
import com.walnutek.fermentationtank.exception.AppException;
import com.walnutek.fermentationtank.model.entity.User;
import com.walnutek.fermentationtank.model.service.LaboratoryService;
import com.walnutek.fermentationtank.model.service.LineNotifyService;
import com.walnutek.fermentationtank.model.service.UserService;
import com.walnutek.fermentationtank.model.vo.LineNotifyVO;
import com.walnutek.fermentationtank.model.vo.Page;
import com.walnutek.fermentationtank.model.vo.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@Tag(name = "LineNotify")
@RestController
@RequestMapping("/line-notify")
public class LineNotifyController {

    @Autowired
    private LineNotifyService lineNotifyService;

    @Autowired
    private UserService userService;

    @Autowired
    private LaboratoryService laboratoryService;

    @Value("${app.line-notify.authorize-api}")
    public String LINE_AUTHORIZE_API;
    public static final String LINE_NOTIFY_API = "/api/line-notify";
    public static final String QUERY_QUESTION = "?";
    public static final String QUERY_AND = "&";
    public static final String QUERY_UNDER_SCORE = "_";
    public static final String QUERY_RESPONSE_TYPE = "response_type=code";
    public static final String QUERY_CLIENT_ID = "client_id=";
    @Value("${app.line-notify.client-id}")
    public String CLIENT_ID;
    public static final String QUERY_REDIRECT_URI = "redirect_uri=";
    public static final String QUERY_ID = "id=";
    public static final String QUERY_SCOPE = "scope=notify";
    public static final String QUERY_STATE = "state=";
    @Value("${app.line-notify.state}")
    public String STATE ;
//    public static final String CrossOrigin = "http://a515-60-248-32-193.ngrok-free.app";

    @Operation(summary = "取得 LineNotify連動 Url")
    @GetMapping("/connection")
    @SecurityRequirement(name = Const.BEARER_JWT)
    @Parameter(name = "laboratoryId", schema = @Schema(implementation = String.class), description = "實驗室Id")
//    @CrossOrigin(origins = CrossOrigin)
    public String getLineNotifyConnectionUrl(
            @Parameter(hidden = true) @RequestParam String laboratoryId
    ){
        var userId = userService.getLoginUserInfo().getUserId();
        var id = laboratoryId + QUERY_UNDER_SCORE + userId;
        var baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        var redirectUri = baseUrl + LINE_NOTIFY_API + QUERY_QUESTION + QUERY_ID + id;
        return LINE_AUTHORIZE_API + QUERY_QUESTION
                + QUERY_RESPONSE_TYPE
                + QUERY_AND + QUERY_CLIENT_ID + CLIENT_ID
                + QUERY_AND + QUERY_REDIRECT_URI + redirectUri
                + QUERY_AND + QUERY_SCOPE
                + QUERY_AND + QUERY_STATE + STATE;
    }

    @Operation(summary = "新增LineNotify綁定")
    @GetMapping
//    @CrossOrigin(origins = CrossOrigin)
    @Parameter(name = "id", schema = @Schema(implementation = String.class), description = "實驗室Id跟使用者Id", example = "65a747eea2df8e3055c98f78_65a74533a2df8e3055c98f77")
    @Parameter(name = "code", schema = @Schema(implementation = String.class), description = "code")
    @Parameter(name = "state", schema = @Schema(implementation = String.class), description = "state")
    public void createLineNotify(
            HttpServletResponse response,
            @Parameter(hidden = true) @RequestParam String id,
            @Parameter(hidden = true) @RequestParam String code,
            @Parameter(hidden = true) @RequestParam String state
    ) throws IOException {
        var idList = Arrays.stream(id.split(QUERY_UNDER_SCORE)).toList();
        if(!idList.isEmpty()){
            var laboratoryId = idList.get(0);
            Optional.ofNullable(laboratoryService.isLabAvailable(laboratoryId))
                    .orElseThrow(() -> new AppException(AppException.Code.E004));

            var userId = idList.get(1);
            Optional.ofNullable(userService.getUserProfile(userId))
                    .orElseThrow(() -> new AppException(AppException.Code.E004));
            var baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
            lineNotifyService.createLineNotify(laboratoryId, userId, baseUrl, code, state);
            response.sendRedirect(baseUrl);
        }else {
            throw new AppException(AppException.Code.E002);
        }
    }

    @Operation(summary = "取得 LineNotify 管理列表")
    @GetMapping("/{laboratoryId}/list")
    @SecurityRequirement(name = Const.BEARER_JWT)
    @Parameter(name = Const.KEYWORD, schema = @Schema(implementation = String.class), description = "使用者名稱")
    @Parameter(name = Const.PAGE, schema = @Schema(implementation = Integer.class), description = "頁數")
    @Parameter(name = Const.LIMIT, schema = @Schema(implementation = Integer.class), description = "每頁幾筆")
    @Parameter(name = Const.SORT_FIELD_KEY, schema = @Schema(implementation = String.class), description = "排序欄位")
    @Parameter(name = Const.SORT_DIRECTION_KEY, schema = @Schema(implementation = String.class), description = "排序方向", example = "asc|desc")
    public Page<LineNotifyVO> search(
            @Parameter(name = "laboratoryId", description = "實驗室ID") @PathVariable String laboratoryId,
            @Parameter(hidden = true) @RequestParam Map<String, Object> paramMap
    ){
        laboratoryService.checkUserIsBelongToLaboratory(laboratoryId, true);
        return lineNotifyService.search(laboratoryId, paramMap);
    }

    @Operation(summary = "更新使用者LineNotify狀態")
    @SecurityRequirement(name = Const.BEARER_JWT)
    @HasRole({User.Role.SUPER_ADMIN, User.Role.LAB_ADMIN})
    @PutMapping("/{laboratoryId}/{lineNotifyId}")
    public Response updateUserLineNotifyStatus(
            @Parameter(name = "laboratoryId", description = "實驗室ID") @PathVariable String laboratoryId,
            @Parameter(name = "lineNotifyId", description = "Line NotifyId ID") @PathVariable String lineNotifyId,
            @RequestBody LineNotifyVO vo) {
        var laboratory = laboratoryService.isLabAvailable(laboratoryId);
        laboratoryService.checkUserIsLaboratoryOwner(laboratory.getOwnerId(), true);
        lineNotifyService.updateLineNotify(laboratoryId, lineNotifyId, vo);
        return Response.ok();
    }
}
