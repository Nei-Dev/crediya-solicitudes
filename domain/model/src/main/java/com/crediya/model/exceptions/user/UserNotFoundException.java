package com.crediya.model.exceptions.user;

import com.crediya.model.exceptions.NotFoundException;

public class UserNotFoundException extends NotFoundException {
	public UserNotFoundException(String message) {
		super(message);
	}
}
