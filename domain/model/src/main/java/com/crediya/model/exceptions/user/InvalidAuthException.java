package com.crediya.model.exceptions.user;

import com.crediya.model.exceptions.BusinessException;

public class InvalidAuthException extends BusinessException {
	public InvalidAuthException(String message) {
		super(message);
	}
}
