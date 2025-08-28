package com.crediya.api.dto.input;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

import static com.crediya.api.constants.ValidationMessage.*;

public record CreateCreditApplicationRequest(
	
	@NotNull(message = AMOUNT_NOT_NULL)
	@Positive(message = AMOUNT_POSITIVE)
	BigDecimal amount,
	
	@NotNull(message = TERM_NOT_NULL)
	@Positive(message = TERM_POSITIVE)
	Integer term,
	
	@NotNull(message = EMAIL_NOT_BLANK)
	@Email(message = EMAIL_FORMAT)
	String email,
	
	@NotNull(message = IDENTIFICATION_NOT_BLANK)
	@NotBlank(message = IDENTIFICATION_NOT_BLANK)
	@Pattern(regexp = "^\\d+$", message = INVALID_IDENTIFICATION)
	@Positive(message = INVALID_IDENTIFICATION)
	String identification,
	
	@NotNull(message = CREDIT_TYPE_ID_NOT_NULL)
	@Positive(message = CREDIT_TYPE_ID_POSITIVE)
	Long idCreditType
	
) {}
