package com.crediya.model.exceptions.pagination;

import com.crediya.model.exceptions.BusinessException;

public class InvalidPaginationFilterException extends BusinessException {
	public InvalidPaginationFilterException(String message) {
		super(message);
	}
}
