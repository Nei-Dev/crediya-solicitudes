package com.crediya.model.creditapplication.ports;

import com.crediya.model.PaginationResponse;
import com.crediya.model.creditapplication.PaginationCreditApplicationFilter;
import com.crediya.model.projection.CreditApplicationProjection;
import reactor.core.publisher.Mono;

public interface IGetCreditApplicationPaginatedUseCase {
	
	Mono<PaginationResponse<CreditApplicationProjection>> execute(PaginationCreditApplicationFilter filter);
	
}
