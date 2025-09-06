package com.crediya.model.projection.pagination;

import com.crediya.model.exceptions.BusinessException;

public class InvalidPaginationFilterException extends BusinessException {
	public InvalidPaginationFilterException(String message) {
		super(message);
	}
}
