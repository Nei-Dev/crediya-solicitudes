package com.crediya.api.config.swagger;

import com.crediya.api.dto.output.ErrorResponse;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springframework.http.HttpStatus;

import static com.crediya.api.constants.swagger.DocApi.*;
import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;

public class ApiDocHelper {
	
	private ApiDocHelper() {}
	
	public static void commonErrorResponse(Builder ops) {
		ops.response(responseBuilder().responseCode(String.valueOf(HttpStatus.BAD_REQUEST.value()))
			.description(DESCRIPTION_BAD_REQUEST)
			.implementation(ErrorResponse.class)).response(responseBuilder().responseCode(String.valueOf(HttpStatus.NOT_FOUND.value()))
			.description(DESCRIPTION_NOT_FOUND)
			.implementation(ErrorResponse.class)).response(responseBuilder().responseCode(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
			.description(DESCRIPTION_INTERNAL_ERROR)
			.implementation(ErrorResponse.class));
	}
	
}
