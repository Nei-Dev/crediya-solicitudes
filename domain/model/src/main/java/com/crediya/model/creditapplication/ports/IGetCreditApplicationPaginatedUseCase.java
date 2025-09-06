package com.crediya.model.creditapplication.ports;

import com.crediya.model.PaginationResponse;
import com.crediya.model.creditapplication.PaginationCreditApplicationFilter;
import com.crediya.model.projection.CreditApplicationSummary;
import reactor.core.publisher.Mono;

public interface IGetCreditApplicationPaginatedUseCase {
	
	Mono<PaginationResponse<CreditApplicationSummary>> execute(PaginationCreditApplicationFilter filter);
	
}
