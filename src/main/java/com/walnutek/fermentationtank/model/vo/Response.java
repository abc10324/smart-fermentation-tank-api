package com.walnutek.fermentationtank.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(title = "通用Response")
@Data
public class Response {

	@Schema(title = "狀態", example = "success|failure")
	private Status status;
	
	private Response(Status status) {
		this.status = status;
	}
	
	public static Response ok() {
		return new Response(Status.success);
	}
	
	public static Response error() {
		return new Response(Status.failure);
	}
	
	private enum Status {
		success, failure
	}
	
}
