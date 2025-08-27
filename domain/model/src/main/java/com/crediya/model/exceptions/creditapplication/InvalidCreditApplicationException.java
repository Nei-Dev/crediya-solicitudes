package com.crediya.model.exceptions.creditapplication;

import com.crediya.model.exceptions.BusinessException;

public class InvalidCreditApplicationException extends BusinessException {
	public InvalidCreditApplicationException(String message) {
		super(message);
	}
}
