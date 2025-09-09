package com.crediya.model.creditapplication.gateways;

import com.crediya.model.PaginationResponse;
import com.crediya.model.creditapplication.CreditApplication;
import com.crediya.model.creditapplication.CreditApplicationSummary;
import com.crediya.model.creditapplication.PaginationCreditApplicationFilter;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface CreditApplicationRepository {
	
	Mono<CreditApplication> saveCreditApplication(CreditApplication creditApplication);
	
	Mono<PaginationResponse<CreditApplicationSummary>> getAllApplications(PaginationCreditApplicationFilter filter);
	
	Mono<CreditApplication> findById(Long id);
	
	Mono<BigDecimal> findTotalMonthlyDebt(String email);
	
}
