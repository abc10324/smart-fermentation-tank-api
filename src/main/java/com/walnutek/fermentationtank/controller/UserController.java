package com.walnutek.fermentationtank.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.walnutek.fermentationtank.config.Const;
import com.walnutek.fermentationtank.config.auth.AuthUser;
import com.walnutek.fermentationtank.config.auth.HasRole;
import com.walnutek.fermentationtank.config.auth.JwtService;
import com.walnutek.fermentationtank.model.entity.User;
import com.walnutek.fermentationtank.model.service.UserService;
import com.walnutek.fermentationtank.model.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@Tag(name = "使用者")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Operation(summary = "取得使用者清單")
    @SecurityRequirement(name = Const.BEARER_JWT)
    @HasRole({User.Role.SUPER_ADMIN, User.Role.LAB_ADMIN})
    @Parameter(name = Const.KEYWORD, schema = @Schema(implementation = String.class), description = "帳號|使用者名稱")
    @Parameter(name = Const.PAGE, schema = @Schema(implementation = Integer.class), description = "頁數")
    @Parameter(name = Const.LIMIT, schema = @Schema(implementation = Integer.class), description = "每頁幾筆")
    @Parameter(name = Const.SORT_FIELD_KEY, schema = @Schema(implementation = String.class), description = "排序欄位")
    @Parameter(name = Const.SORT_DIRECTION_KEY, schema = @Schema(implementation = String.class), description = "排序方向", example = "asc|desc")
    @GetMapping
    public Page<UserVO> search(@Parameter(hidden = true) @RequestParam Map<String, Object> paramMap){
        return userService.search(paramMap);
    }

    @Operation(summary = "登入")
    @PostMapping("/login")
    public LoginUserInfo login(@RequestBody LoginInfo loginInfo) {
        var user = userService.UserLoginCheck(loginInfo.getAccount(), loginInfo.getPassword());
        var token = jwtService.generateToken(user);

        return LoginUserInfo.of(token, user);
    }

    @Operation(summary = "取得登入使用者基本資訊")
    @SecurityRequirement(name = Const.BEARER_JWT)
    @HasRole({User.Role.SUPER_ADMIN, User.Role.LAB_ADMIN})
    @GetMapping("/info")
    public LoginUserInfo getUserInfo(){
        return LoginUserInfo.of(userService.getLoginUserInfo());
    }

	@Operation(summary = "取得登入使用者個人頁面資訊")
	@SecurityRequirement(name = Const.BEARER_JWT)
    @HasRole({User.Role.SUPER_ADMIN, User.Role.LAB_ADMIN})
	@GetMapping("/profile")
	public UserVO getUserProfile(){
		return userService.getUserProfile();
	}

	@Operation(summary = "檢查帳號是否存在")
	@GetMapping("/exist/{account}")
	public AccountExistResponse isAccountExist(
            @Parameter(name = "account", description = "使用者帳號") @PathVariable String account)
    {
		return AccountExistResponse.of(userService.isAccountExist(account));
	}

    @Operation(summary = "新增使用者")
    @SecurityRequirement(name = Const.BEARER_JWT)
    @HasRole({User.Role.SUPER_ADMIN, User.Role.LAB_ADMIN})
    @PostMapping
    public Response createUser(@RequestBody UserVO vo) {
        userService.createUser(vo);
        return Response.ok();
    }

    @Operation(summary = "更新使用者基本資料", description = "更新自己的資料")
	@SecurityRequirement(name = Const.BEARER_JWT)
	@PutMapping
	public Response updateCurrentUser(@RequestBody UserVO vo) {
		userService.updateUser(vo);
		return Response.ok();
	}

	@Operation(summary = "更新使用者基本資料", description = "更新別人的資料")
	@SecurityRequirement(name = Const.BEARER_JWT)
	@HasRole({User.Role.SUPER_ADMIN, User.Role.LAB_ADMIN})
	@PutMapping("/{userId}")
	public Response updateUser(@Parameter(name = "userId", description = "使用者ID")
							   @PathVariable String userId,
							   @RequestBody UserVO vo
    ) {
		userService.updateUser(userId, vo);
		return Response.ok();
	}

    @Operation(summary = "更新密碼", description = "更新該登入使用者的密碼")
    @SecurityRequirement(name = Const.BEARER_JWT)
    @HasRole({User.Role.SUPER_ADMIN, User.Role.LAB_ADMIN})
    @PutMapping("/changePassword")
    public Response changePassword(@RequestBody ChangePasswordPayload paylaod) {
        userService.updateUserPassword(paylaod.getOldPassword(), paylaod.getNewPassword());
        return Response.ok();
    }

    @Operation(summary = "刪除使用者", description = "刪除該使用者")
    @SecurityRequirement(name = Const.BEARER_JWT)
    @HasRole({User.Role.SUPER_ADMIN, User.Role.LAB_ADMIN})
    @DeleteMapping("/{userId}")
    public Response deleteUser(@Parameter(name = "userId", description = "使用者ID")
                               @PathVariable String userId){
        userService.updateUserStatus(userId, User.Status.DELETED);
        return Response.ok();
    }

    @Operation(summary = "取得自己可用的實驗室清單")
    @SecurityRequirement(name = Const.BEARER_JWT)
    @GetMapping("/{userId}/option/availableLab")
    public List<OptionVO> getAvailableLabOptionList(@Parameter(description = "使用者ID") @PathVariable("userId") String userId) {
        return userService.getAvailableLabList(userId)
                .stream()
                .map(vo -> OptionVO.of(vo.getId(), vo.getName()))
                .toList();
    }

    @Operation(summary = "取得自己所屬的實驗室清單")
    @SecurityRequirement(name = Const.BEARER_JWT)
    @GetMapping("/option/ownLab")
    public List<OptionVO> getOwnLabOptionList() {
        return userService.getOwnLabList()
                    .stream()
                    .map(vo -> OptionVO.of(vo.getId(), vo.getName()))
                    .toList();
    }

    @Schema(title = "登入資訊")
    @Data
    private static class LoginInfo {

        @Schema(title = "帳號")
        private String account;

        @Schema(title = "密碼")
        private String password;

    }

    @Schema(title = "登入使用者資訊")
    @Data
    private static class LoginUserInfo {

        @Schema(title = "JWT token")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String token;

        @Schema(title = "帳號")
        private String account;

        @Schema(title = "使用者角色")
        private User.Role role;

        @Schema(title = "使用者暱稱")
        private String name;

        @Schema(title = "Email")
        private String email;

        @Schema(title = "所屬實驗室ID清單")
        private List<String> labList = List.of();

        public static LoginUserInfo of(String token,
                                       AuthUser user) {
            var info = new LoginUserInfo();
            info.token = token;
            info.account = user.getAccount();
            info.role = user.getRole();
            info.name = user.getName();
            info.email = user.getEmail();
            info.labList = user.getLabList();

            return info;
        }

        public static LoginUserInfo of(AuthUser user) {
            var info = new LoginUserInfo();
            info.account = user.getAccount();
            info.role = user.getRole();
            info.name = user.getName();
            info.email = user.getEmail();
            info.labList = user.getLabList();

            return info;
        }
    }

    @Schema(title = "密碼變更資料載體")
    @Data
    private static class ChangePasswordPayload {

        @Schema(title = "舊密碼")
        private String oldPassword;

        @Schema(title = "新密碼")
        private String newPassword;
    }

    @Schema(title = "帳號存在檢查回傳資料")
	@Data
	private static class AccountExistResponse {

		@Schema(title = "帳號是否存在")
		private Boolean isAccountExist;

		public static AccountExistResponse of(Boolean isAccountExist) {
			var vo = new AccountExistResponse();
			vo.isAccountExist = isAccountExist;

			return vo;
		}
	}

}
