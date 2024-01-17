package com.walnutek.fermentationtank.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;
import lombok.Data;
import lombok.Getter;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.time.LocalDateTime;

/**
 * Extending this class to have below fields and use system auto update mechanism to deal with it
 *
 * if you use @Data , please add @EqualsAndHashCode(callSuper = false) for dismiss the warning
 * @author Walnutek-Sam
 *
 */
@Data
public class BaseColumns {

	/**
	 * PK
	 */
	@Schema(title = "主鍵", description = "primary key", accessMode = AccessMode.READ_ONLY)
	@Id
	@JsonInclude(Include.NON_NULL)
	private String id;

	/**
	 * 新增資料時間
	 */
	@Schema(title = "新增資料時間", accessMode = AccessMode.READ_ONLY)
	@JsonFormat(pattern = "yyyy/MM/dd HH:mm")
	@CreatedDate
	@JsonInclude(Include.NON_NULL)
	private LocalDateTime createTime;

	/**
	 * 新增資料的使用者
	 */
	@Schema(title = "新增資料的使用者", accessMode = AccessMode.READ_ONLY)
	@CreatedBy
	@Field(targetType = FieldType.OBJECT_ID)
	@JsonInclude(Include.NON_NULL)
	private String createUser;

	/**
	 * 更新資料時間
	 */
	@Schema(title = "更新資料時間", accessMode = AccessMode.READ_ONLY)
	@JsonFormat(pattern = "yyyy/MM/dd HH:mm")
	@LastModifiedDate
	@JsonInclude(Include.NON_NULL)
	private LocalDateTime updateTime;

	/**
	 * 更新資料次數
	 */
	@Schema(title = "更新資料次數", accessMode = AccessMode.READ_ONLY)
	@Version
	private long updateCount = 0L;

	/**
	 * 更新資料的使用者
	 */
	@Schema(title = "更新資料的使用者", accessMode = AccessMode.READ_ONLY)
	@LastModifiedBy
	@Field(targetType = FieldType.OBJECT_ID)
	@JsonInclude(Include.NON_NULL)
	private String updateUser;

	public static <S extends BaseColumns,T extends BaseColumns> void syncBaseColumns(S source,T target) {
		target.setId(source.getId());
		target.setCreateTime(source.getCreateTime());
		target.setCreateUser(source.getCreateUser());
		target.setUpdateTime(source.getUpdateTime());
		target.setUpdateUser(source.getUpdateUser());
	}

	@Getter
	public enum Status {
		DELETED( "刪除"),
		ACTIVE("啟用");

		private String name;

		Status(String name) {
			this.name = name;
		}
	}

}
