package com.crediya.model.creditapplication.ports;

import com.crediya.model.PaginationResponse;
import com.crediya.model.creditapplication.CreditApplicationSummary;
import com.crediya.model.creditapplication.PaginationCreditApplicationFilter;
import reactor.core.publisher.Mono;

public interface IGetCreditApplicationPaginatedUseCase {
	
	Mono<PaginationResponse<CreditApplicationSummary>> execute(PaginationCreditApplicationFilter filter);
	
}
