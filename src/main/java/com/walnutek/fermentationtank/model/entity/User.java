package com.walnutek.fermentationtank.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.walnutek.fermentationtank.config.Const;
import com.walnutek.fermentationtank.exception.AppException;
import com.walnutek.fermentationtank.exception.AppException.Code;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 使用者
 * @author Walnutek-Sam
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Document(Const.COLLECTION_USER)
public class User extends BaseColumns {

	/**
	 * 管理者ID(實驗室使用者填入實驗室管理者的ID)
	 */
	private String adminId;

	/**
	 * 帳號
	 */
	@Indexed(unique = true)
	private String account;

	/**
	 * 密碼
	 */
	private String password;

	/**
	 * 使用者類別
	 */
	private Role role;
	
	/**
	 * 使用者名稱
	 */
	private String name;

	/**
	 * Email
	 */
	private String email;

	/**
	 * 所屬實驗室
	 */
	@Field(targetType = FieldType.OBJECT_ID)
	private List<String> labList = List.of();

	/**
	 * 帳號狀態
	 */
	private Status status = Status.ACTIVE;
	
	/**
	 * 登入次數
	 */
	private Integer loginCount = 0;
	
	/**
	 * 最後登入時間
	 */
	@JsonFormat(pattern = "yyyy/MM/dd HH:mm")
	private LocalDateTime lastLoginTime;

	@Getter
	public enum Status {
		DELETED( "刪除"),
		ACTIVE("啟用");
		
		private String name;
		
		Status(String name) {
			this.name = name;
		}
	}
	
	@Getter
	public enum Role {
		SUPER_ADMIN("超級管理者"),
		LAB_ADMIN("實驗室管理者"),
		LAB_USER("實驗室員工");

		private String name;

		Role(String name) {
			this.name = name;
		}

		public static Role parse(String roleStr) {
			Role result = null;
			
			for(Role role : Role.values()) {
				if(role.name().toLowerCase().equals(roleStr.toLowerCase())) {
					result = role;
				}
			}
			
			return Optional.ofNullable(result)
						   .orElseThrow(() -> new AppException(Code.E000, "錯誤的使用者類別"));
		}
	}
	
}
