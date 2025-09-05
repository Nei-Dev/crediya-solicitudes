package com.crediya.model;

import java.util.List;

public class PaginationResponse<T> {
	
	private List<T> content;
	private int page;
	private int size;
	private long totalElements;
	private int totalPages;
	
}
