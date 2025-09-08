package com.crediya.model.exceptions.statecreditapplication;

import com.crediya.model.exceptions.BusinessException;

public class InvalidStateCreditApplication extends BusinessException {
	public InvalidStateCreditApplication(String message) {
		super(message);
	}
}
