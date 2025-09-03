package com.crediya.api.openapi;

import com.crediya.api.dto.input.CreateCreditApplicationRequest;
import com.crediya.api.dto.output.creditapplication.CreditApplicationApiResponse;
import com.crediya.api.helpers.ApiDocHelper;
import lombok.experimental.UtilityClass;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;

@UtilityClass
public class CreditApplicationDocApi {
	
	public static final String TAG_CREDIT_APPLICATION = "Credit Application";
	
	// Create Credit Application
	public static final String OPERATION_ID_CREATE = "createCreditApplication";
	public static final String SUMMARY_CREATE = "Create a new credit application in the system";
	public static final String DESCRIPTION_CREATED = "Credit application created successfully";
	
	public void createCreditApplicationDoc(Builder builder) {
		ApiDocHelper.commonErrorResponse(
			builder
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
				)
		);
	}
	
}
