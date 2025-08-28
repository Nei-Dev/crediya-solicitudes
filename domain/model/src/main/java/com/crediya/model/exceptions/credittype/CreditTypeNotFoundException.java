package com.crediya.model.exceptions.credittype;

import com.crediya.model.exceptions.NotFoundException;

public class CreditTypeNotFoundException extends NotFoundException {
	public CreditTypeNotFoundException(String message) {
		super(message);
	}
}
