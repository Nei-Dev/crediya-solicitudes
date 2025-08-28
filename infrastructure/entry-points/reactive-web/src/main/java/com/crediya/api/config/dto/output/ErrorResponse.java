package com.crediya.api.config.dto.output;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
public class ErrorResponse {
	
	private String message;
	private int code;
	
	private ErrorResponse(String message, int code) {
		this.message = message;
		this.code = code;
	}
	
	public static ErrorResponse badRequest(String message) {
		return new ErrorResponse(message, HttpStatus.BAD_REQUEST.value());
	}
	
	public static ErrorResponse notFound(String message) {
		return new ErrorResponse(message, HttpStatus.NOT_FOUND.value());
	}
	
	public static ErrorResponse internalServerError(String message) {
		return new ErrorResponse(message, HttpStatus.INTERNAL_SERVER_ERROR.value());
	}
}
