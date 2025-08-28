package com.crediya.api.constants;

public final class ValidationMessage {

    private ValidationMessage() {}

    public static final String AMOUNT_NOT_NULL = "Amount cannot be null";
    public static final String AMOUNT_POSITIVE = "Amount must be a positive number";
    public static final String TERM_NOT_NULL = "Term cannot be null";
    public static final String TERM_POSITIVE = "Term must be a positive number";
    public static final String EMAIL_NOT_BLANK = "Email cannot be blank";
    public static final String EMAIL_FORMAT = "Email must have a valid format";
    public static final String IDENTIFICATION_NOT_BLANK = "Identification is required and cannot be blank";
    public static final String INVALID_IDENTIFICATION = "Identification is invalid";
    public static final String CREDIT_TYPE_ID_NOT_NULL = "Credit type ID cannot be null";
    public static final String CREDIT_TYPE_ID_POSITIVE = "Credit type ID must be a positive number";

}