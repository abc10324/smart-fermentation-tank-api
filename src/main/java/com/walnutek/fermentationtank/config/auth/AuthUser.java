package com.walnutek.fermentationtank.config.auth;

import com.walnutek.fermentationtank.model.entity.User;
import io.jsonwebtoken.Claims;
import lombok.Data;
import org.bson.types.ObjectId;

import java.util.List;

@Data
public class AuthUser {

	private String userId;

	private User.Role role;

	private String account;
	
	private String name;

	private String email;

	private List<String> labList = List.of();

	public static AuthUser of(Claims claims) {
		AuthUser user = new AuthUser();
		user.userId = String.valueOf(claims.get("userId"));
		user.role = User.Role.parse(String.valueOf(claims.get("role")));
		user.account = (String) claims.get("account");
		user.name = (String) claims.get("name");
		user.email = (String) claims.get("email");

		return user;
	}

	public static AuthUser of(User data) {
		var vo = new AuthUser();
		vo.setUserId(data.getId());
		vo.setAccount(data.getAccount());
		vo.setRole(data.getRole());
		vo.setName(data.getName());
		vo.setEmail(data.getEmail());
		vo.setLabList(data.getLabList());

		return vo;
	}
}
