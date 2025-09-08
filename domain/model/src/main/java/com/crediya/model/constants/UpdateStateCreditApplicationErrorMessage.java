package com.crediya.model.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class UpdateStateCreditApplicationErrorMessage {
	
	public static final String STATE_NOT_BLANK = "State is required cannot be null";
	public static final String STATE_INVALID = "State is invalid. Valid states are: APPROVED, REJECTED";
	public static final String STATE_CANNOT_BE_MODIFIED = "State in credit application cannot be modified";
	
}