package com.crediya.api.openapi;

import com.crediya.api.dto.input.CreateCreditApplicationRequest;
import com.crediya.api.dto.output.creditapplication.CreditApplicationApiResponse;
import com.crediya.api.dto.output.creditapplication.CreditApplicationPaginationResponse;
import com.crediya.api.helpers.ApiDocHelper;
import com.crediya.model.creditapplication.StateCreditApplication;
import com.crediya.model.helpers.SortDirection;
import lombok.experimental.UtilityClass;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static com.crediya.api.constants.PaginationParams.*;
import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;

@UtilityClass
public class CreditApplicationDocApi {
	
	public static final String TAG_CREDIT_APPLICATION = "Credit Application";
	
	// Create Credit Application
	public static final String OPERATION_ID_CREATE = "createCreditApplication";
	public static final String SUMMARY_CREATE = "Create a new credit application in the system";
	public static final String DESCRIPTION_CREATED = "Credit application created successfully";
	
	// List Credit Application
	public static final String OPERATION_ID_LIST = "listCreditApplication";
	public static final String SUMMARY_LIST = "List all credit applications in the system";
	public static final String PARAM_PAGE_DESCRIPTION = "Page number (starting from 1). Default is 1";
	public static final String PARAM_SIZE_DESCRIPTION = "Number of records per page. Default is 10";
	public static final String PARAM_SORT_BY_DESCRIPTION = "Field to sort by. Default is 'amount'";
	public static final String PARAM_DIRECTION_DESCRIPTION = "Sort direction. Default is 'ASC'";
	public static final String PARAM_STATUS_DESCRIPTION = "Filter by credit application status";
	public static final String PARAM_AUTO_EVALUATION_DESCRIPTION = "Filter by auto evaluation result";
	
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
	
	public void listCreditApplicationDoc(Builder builder) {
		ApiDocHelper.commonErrorResponse(
			builder
				.summary(SUMMARY_LIST)
				.operationId(OPERATION_ID_LIST)
				.tag(TAG_CREDIT_APPLICATION)
				.parameter(
					parameterBuilder()
						.name(PARAM_PAGE)
						.description(PARAM_PAGE_DESCRIPTION)
						.implementation(Integer.class)
				)
				.parameter(
					parameterBuilder()
						.name(PARAM_SIZE)
						.description(PARAM_SIZE_DESCRIPTION)
						.implementation(Integer.class)
				)
				.parameter(
					parameterBuilder()
						.required(false)
						.name(PARAM_SORT_BY)
						.description(PARAM_SORT_BY_DESCRIPTION)
				)
				.parameter(
					parameterBuilder()
						.required(false)
						.name(PARAM_DIRECTION)
						.implementation(SortDirection.class)
						.description(PARAM_DIRECTION_DESCRIPTION)
				)
				.parameter(
					parameterBuilder()
						.required(false)
						.name(PARAM_STATUS)
						.implementation(StateCreditApplication.class)
						.description(PARAM_STATUS_DESCRIPTION)
				)
				.parameter(
					parameterBuilder()
						.required(false)
						.name(PARAM_AUTO_EVALUATION)
						.implementation(Boolean.class)
						.description(PARAM_AUTO_EVALUATION_DESCRIPTION)
				)
				.response(responseBuilder()
					.responseCode(String.valueOf(HttpStatus.OK.value()))
					.content(contentBuilder()
						.mediaType(MediaType.APPLICATION_JSON_VALUE)
						.schema(schemaBuilder()
							.implementation(CreditApplicationPaginationResponse.class)
						)
					)
				)
		);
	}
	
}
