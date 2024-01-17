package com.walnutek.fermentationtank.model.service;

import com.walnutek.fermentationtank.exception.AppException;
import com.walnutek.fermentationtank.exception.AppException.Code;
import org.springframework.data.mongodb.core.aggregation.Field;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.util.ArrayUtils;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

public class Utils {

	public static Field mappingField(String ...field) {
		return Fields.field(String.join(".", field));
	}

	public static Boolean hasText(Object value) {
		return StringUtils.hasText(Optional.ofNullable(value).map(String::valueOf).orElse(""));
	}

	public static Boolean hasArray(Object value) {
		var result = false;
		if (value.getClass().isArray()) {
			var list = Arrays.asList((Object[])value);
			if(!list.isEmpty()){
				result = true;
			}
		}
		return result;
	}

	public static String id() {
		return "_id";
	}

	public static <T> String field(SFunction<T,?> fieldGetter) {
		var methodName = getMethodName(fieldGetter);

		if(methodName.startsWith("get")) {
			methodName.substring(3);
			char[] charArray = methodName.substring(3).toCharArray();
			charArray[0] = Character.toLowerCase(charArray[0]);

			var result = new String(charArray);

			if("id".equals(result)) {
				result = "_id";
			}

			return result;
		} else {
			throw new AppException(Code.E000);
		}
	}

	private static String getMethodName(Serializable lambda) {
		try {
			Method m = lambda.getClass().getDeclaredMethod("writeReplace");
			m.setAccessible(true);
			SerializedLambda sl = (SerializedLambda) m.invoke(lambda);
			return sl.getImplMethodName();
		} catch (ReflectiveOperationException ex) {
			throw new RuntimeException(ex);
		}
	}

	@FunctionalInterface
	public interface SFunction<T, V> extends Function<T, V>, Serializable {
	}
}
