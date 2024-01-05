package com.walnutek.fermentationtank.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.walnutek.fermentationtank.config.Const;
import com.walnutek.fermentationtank.config.auth.Auth;
import com.walnutek.fermentationtank.config.auth.AuthUser;
import com.walnutek.fermentationtank.config.auth.HasRole;
import com.walnutek.fermentationtank.config.auth.JwtService;
import com.walnutek.fermentationtank.exception.AppException;
import com.walnutek.fermentationtank.exception.AppException.Code;
import com.walnutek.fermentationtank.model.entity.User;
import com.walnutek.fermentationtank.model.service.UserService;
import com.walnutek.fermentationtank.model.service.Utils;
import com.walnutek.fermentationtank.model.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

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
    @Parameter(name = "keyword", schema = @Schema(implementation = String.class), description = "帳號|使用者名稱")
    @Parameter(name = "page", schema = @Schema(implementation = Integer.class), description = "頁數")
    @Parameter(name = "limit", schema = @Schema(implementation = Integer.class), description = "每頁幾筆")
    @Parameter(name = "orderBy", schema = @Schema(implementation = String.class), description = "排序欄位")
    @Parameter(name = "sort", schema = @Schema(implementation = String.class), description = "排序方向", example = "asc|desc")
    @GetMapping
    public Page<UserVO> search(@Parameter(hidden = true) @RequestParam Map<String, Object> paramMap){
        return userService.search(paramMap);
    }

    @Operation(summary = "登入")
    @PostMapping("/login")
    public LoginUserInfo login(@RequestBody LoginInfo loginInfo) {
        var user = userService.getLoginUser(loginInfo.getAccount(), loginInfo.getPassword());
        var token = jwtService.generateToken(user);

        return LoginUserInfo.of(token, user);
    }

    @Operation(summary = "取得登入使用者基本資訊")
    @SecurityRequirement(name = Const.BEARER_JWT)
    @GetMapping("/info")
    public LoginUserInfo getUserInfo(){
        return LoginUserInfo.of(userService.getLoginUserInfo());
    }
    
	@Operation(summary = "取得登入使用者個人頁面資訊")
	@SecurityRequirement(name = Const.BEARER_JWT)
	@GetMapping("/profile")
	public UserVO getUserProfile(){
		return userService.getUserProfile();
	}
	
	@Operation(summary = "檢查帳號是否存在")
	@GetMapping("/exist/{account}")
	public AccountExistResponse isAccountExist(@Parameter(name = "account", description = "使用者帳號") 
											   @PathVariable String account){
		return AccountExistResponse.of(userService.isAccountExist(account));
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
							   @RequestBody UserVO vo) {
		userService.updateUser(userId, vo);
		return Response.ok();
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
        private List<String> labIdList = List.of();

        public static LoginUserInfo of(String token,
                                       AuthUser user) {
            var info = new LoginUserInfo();
            info.token = token;
            info.account = user.getAccount();
            info.role = user.getRole();
            info.name = user.getName();
            info.email = user.getEmail();
            info.labIdList = user.getLabIdList();

            return info;
        }

        public static LoginUserInfo of(AuthUser user) {
            var info = new LoginUserInfo();
            info.account = user.getAccount();
            info.role = user.getRole();
            info.name = user.getName();
            info.email = user.getEmail();
            info.labIdList = user.getLabIdList();

            return info;
        }
    }
    
    @Schema(title = "註冊使用者資料載體", accessMode = Schema.AccessMode.WRITE_ONLY)
    @Data
    private static class RegistUserPayload {
    	
    	@Schema(title = "帳號")
    	private String account;
    	
    	@Schema(title = "密碼")
    	private String password;

		@Schema(title = "使用者類別")
		private User.Role role;
    	
    	@Schema(title = "使用者名稱")
    	private String name;

		@Schema(title = "Email")
		private String email;

		@Schema(title = "所屬實驗室ID清單")
		private List<String> labIdList = List.of();
    	
    	public UserVO toUserVo() {
    		var vo = new UserVO();
    		vo.setAccount(account);
    		vo.setPassword(password);
    		vo.setName(name);
			vo.setEmail(email);
			vo.setLabIdList(labIdList);
            vo.setStatus(User.Status.ACTIVE);
    		
    		return vo;
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
