package com.crediya.api.helpers;

import com.crediya.api.dto.output.ErrorResponse;
import lombok.experimental.UtilityClass;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springframework.http.HttpStatus;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;

@UtilityClass
public class ApiDocHelper {
	
	public final String DESCRIPTION_BAD_REQUEST = "Invalid input data";
	public final String DESCRIPTION_NOT_FOUND = "Resource not found";
	public final String DESCRIPTION_INTERNAL_ERROR = "Internal server error";
	
	public void commonErrorResponse(Builder ops) {
		ops
			.response(
				responseBuilder()
					.responseCode(String.valueOf(HttpStatus.BAD_REQUEST.value()))
					.description(DESCRIPTION_BAD_REQUEST)
					.implementation(ErrorResponse.class)
			)
			.response(
				responseBuilder()
					.responseCode(String.valueOf(HttpStatus.NOT_FOUND.value()))
					.description(DESCRIPTION_NOT_FOUND)
					.implementation(ErrorResponse.class)
			)
			.response(
				responseBuilder()
					.responseCode(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
					.description(DESCRIPTION_INTERNAL_ERROR)
					.implementation(ErrorResponse.class)
			);
	}
	
}
