package com.walnutek.fermentationtank.config.auth;



import com.walnutek.fermentationtank.model.entity.User;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 限制class或method能使用的使用者
 * class與method可同時標註, 若其中一則違反規則就丟出Exception
 * @author Walnutek-Sam
 *
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface HasRole {

	/**
	 * 可使用的使用者
	 * 等同於roles欄位
	 * @return
	 */
	User.Role[] value() default {};
	
	/**
	 * 可使用的使用者
	 * @return
	 */
	User.Role[] roles() default {};
	
}
