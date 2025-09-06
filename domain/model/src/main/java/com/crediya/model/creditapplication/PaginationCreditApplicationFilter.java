package com.crediya.model.creditapplication;

import com.crediya.model.helpers.SortDirection;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaginationCreditApplicationFilter {
	
	private int page;
	private int size;
	private String sortBy;
	private SortDirection direction;
	private StateCreditApplication status;
	private Boolean autoEvaluation;
	
}
