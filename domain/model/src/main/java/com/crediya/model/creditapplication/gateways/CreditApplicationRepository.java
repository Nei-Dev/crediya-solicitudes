package com.crediya.model.creditapplication.gateways;

import com.crediya.model.PaginationResponse;
import com.crediya.model.creditapplication.CreditApplication;
import com.crediya.model.creditapplication.PaginationCreditApplicationFilter;
import com.crediya.model.projection.CreditApplicationSummary;
import reactor.core.publisher.Mono;

public interface CreditApplicationRepository {
	
	Mono<CreditApplication> createApplication(CreditApplication creditApplication);
	
	Mono<PaginationResponse<CreditApplicationSummary>> getAllApplications(PaginationCreditApplicationFilter filter);
	
}
