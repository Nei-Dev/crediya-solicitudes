package com.crediya.r2dbc.exceptions;

import com.crediya.model.exceptions.TechnicalException;

public class StateNotFoundException extends TechnicalException {
	public StateNotFoundException(String message) {
		super(message);
	}
}
