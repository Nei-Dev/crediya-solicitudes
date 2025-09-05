package com.crediya.model.creditapplication;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaginationCreditApplicationFilter {
	
	private int page;
	private int size;
	private String sortebBy;
	private StateCreditApplication status;
	private Boolean autoEvaluation;
}
