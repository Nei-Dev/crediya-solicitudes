package com.crediya.api.dto.output;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseDTO<T> {
	protected T data;
	protected String message;
	
	public static <T> ApiResponseDTO<T> of(T data, String message) {
		return ApiResponseDTO.<T>builder()
				.data(data)
				.message(message)
				.build();
	}
}
