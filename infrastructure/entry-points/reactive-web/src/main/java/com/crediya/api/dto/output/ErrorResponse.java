package com.crediya.api.dto.output;

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
	
	public static ErrorResponse forbidden(String message) {
		return new ErrorResponse(message, HttpStatus.FORBIDDEN.value());
	}
	
	public static ErrorResponse unauthorized(String message) {
		return new ErrorResponse(message, HttpStatus.UNAUTHORIZED.value());
	}
}
