package com.crediya.model.constants;

public class ErrorMessage {
	private ErrorMessage() {}
	
	public static final String NULL_CREDIT_APPLICATION = "Credit application cannot be null";
	public static final String INVALID_AMOUNT_REQUESTED = "Amount requested is required and must be greater than zero";
	public static final String AMOUNT_REQUESTED_OUT_OF_RANGE = "Amount requested is not within the allowed limit based on the credit type";
	public static final String INVALID_TERM_IN_MONTHS = "Term in months is required and must be greater than zero";
	public static final String INVALID_ID_CREDIT_TYPE = "Id credit type is required and must be greater than zero";
	public static final String CREDIT_TYPE_NOT_FOUND = "Credit type not found";
	public static final String INVALID_EMAIL = "Email is required and must be a valid email format";
	public static final String INVALID_IDENTIFICATION = "Identification number is required and cannot be empty";
	public static final String USER_NOT_MATCH = "User information does not match";
	public static final String USER_NOT_FOUND = "User not found";
	
}
