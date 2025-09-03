package com.crediya.api.dto.output;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
	protected T data;
	protected String message;
	
	public static <T> ApiResponse<T> of(T data, String message) {
		return ApiResponse.<T>builder()
				.data(data)
				.message(message)
				.build();
	}
}
