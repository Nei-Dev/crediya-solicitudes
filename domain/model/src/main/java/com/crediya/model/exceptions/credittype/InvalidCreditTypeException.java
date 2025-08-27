package com.crediya.model.exceptions.credittype;

import com.crediya.model.exceptions.BusinessException;

public class InvalidCreditTypeException extends BusinessException {
	public InvalidCreditTypeException(String message) {
		super(message);
	}
}
