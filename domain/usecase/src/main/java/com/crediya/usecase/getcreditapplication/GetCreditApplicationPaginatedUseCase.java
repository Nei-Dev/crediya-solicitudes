package com.crediya.usecase.getcreditapplication;

import com.crediya.model.PaginationResponse;
import com.crediya.model.creditapplication.PaginationCreditApplicationFilter;
import com.crediya.model.creditapplication.gateways.CreditApplicationRepository;
import com.crediya.model.creditapplication.ports.IGetCreditApplicationPaginatedUseCase;
import com.crediya.model.projection.CreditApplicationProjection;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class GetCreditApplicationPaginatedUseCase implements IGetCreditApplicationPaginatedUseCase {
	
	private final CreditApplicationRepository repository;
	
	@Override
	public Mono<PaginationResponse<CreditApplicationProjection>> execute(PaginationCreditApplicationFilter filter) {
		return null;
	}
	
}
