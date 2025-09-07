package com.crediya.model.exceptions.creditapplication;

import com.crediya.model.exceptions.NotFoundException;

public class CreditApplicationNotFoundException extends NotFoundException {
	public CreditApplicationNotFoundException(String message) {
		super(message);
	}
}
