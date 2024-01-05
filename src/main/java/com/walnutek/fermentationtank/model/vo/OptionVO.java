package com.walnutek.fermentationtank.model.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.walnutek.fermentationtank.exception.AppException;
import com.walnutek.fermentationtank.exception.AppException.Code;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Objects;

@Schema(title = "選項")
@Data
public class OptionVO {

	@Schema(title = "鍵值")
	private String key;
	
	@Schema(title = "名稱")
	private String name;
	
	@Schema(title = "子選項清單")
	@JsonInclude(Include.NON_NULL)
	private List<OptionVO> childOptionList;
	
	public static OptionVO of(Object key, String name) {
		if(Objects.isNull(key)) {
			throw new AppException(Code.E006);
		}
		
		var vo = new OptionVO();
		vo.key = String.valueOf(key);
		vo.name = name;
		
		return vo;
	}
	
	public static OptionVO of(Object key, String name, List<OptionVO> childOptionList) {
		if(Objects.isNull(key)) {
			throw new AppException(Code.E006);
		}
		
		var vo = new OptionVO();
		vo.key = String.valueOf(key);
		vo.name = name;
		vo.childOptionList = childOptionList;
		
		return vo;
	}
}
