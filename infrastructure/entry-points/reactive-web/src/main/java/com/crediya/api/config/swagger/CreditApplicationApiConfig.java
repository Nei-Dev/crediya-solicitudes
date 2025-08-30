package com.crediya.api.config.swagger;

import com.crediya.api.dto.input.CreateCreditApplicationRequest;
import com.crediya.api.dto.output.creditapplication.CreditApplicationApiResponse;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static com.crediya.api.constants.swagger.creditapplication.CreditApplicationDocApi.*;
import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;

public class CreditApplicationApiConfig {
	
	private CreditApplicationApiConfig() {}
	
	public static Builder createCreditApplicationDoc(Builder builder) {
		return builder
			.summary(SUMMARY_CREATE)
			.operationId(OPERATION_ID_CREATE)
			.tag(TAG_CREDIT_APPLICATION)
			.requestBody(requestBodyBuilder()
				.required(true)
				.content(contentBuilder()
					.mediaType(MediaType.APPLICATION_JSON_VALUE)
					.schema(schemaBuilder()
						.implementation(CreateCreditApplicationRequest.class)
					)
				)
			)
			.response(responseBuilder()
				.responseCode(String.valueOf(HttpStatus.CREATED.value()))
				.description(DESCRIPTION_CREATED)
				.content(contentBuilder()
					.mediaType(MediaType.APPLICATION_JSON_VALUE)
					.schema(schemaBuilder()
						.implementation(CreditApplicationApiResponse.class)
					)
				)
			);
	}
}
