package com.crediya.api.dto.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import static com.crediya.api.constants.ValidationMessage.*;

public record UpdateStateCreditApplication(
	
	@NotNull(message = ID_CREDIT_APPLICATION_NOT_NULL)
	@Positive(message = ID_CREDIT_APPLICATION_POSITIVE)
	Long idCreditApplication,
	
	@NotNull(message = STATE_NOT_BLANK)
	@NotBlank(message = STATE_NOT_BLANK)
	String state
) {
}
