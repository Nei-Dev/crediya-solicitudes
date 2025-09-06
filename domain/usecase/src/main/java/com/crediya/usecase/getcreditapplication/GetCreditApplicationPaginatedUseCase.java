package com.crediya.usecase.getcreditapplication;

import com.crediya.model.PaginationResponse;
import com.crediya.model.creditapplication.CreditApplicationSummary;
import com.crediya.model.creditapplication.PaginationCreditApplicationFilter;
import com.crediya.model.creditapplication.gateways.CreditApplicationRepository;
import com.crediya.model.creditapplication.ports.IGetCreditApplicationPaginatedUseCase;
import com.crediya.model.exceptions.pagination.InvalidPaginationFilterException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import static com.crediya.model.constants.ListCreditApplicationErrorMessage.INVALID_PAGE;
import static com.crediya.model.constants.ListCreditApplicationErrorMessage.INVALID_PAGINATION_FILTER;

@RequiredArgsConstructor
public class GetCreditApplicationPaginatedUseCase implements IGetCreditApplicationPaginatedUseCase {
	
	private final CreditApplicationRepository repository;
	
	@Override
	public Mono<PaginationResponse<CreditApplicationSummary>> execute(PaginationCreditApplicationFilter filter) {
		return this.validateFilter(filter)
			.flatMap(repository::getAllApplications);
	}
	
	private Mono<PaginationCreditApplicationFilter> validateFilter(PaginationCreditApplicationFilter filter) {
		return Mono.defer(() -> {
			if (filter == null) {
				return Mono.error(new InvalidPaginationFilterException(INVALID_PAGINATION_FILTER));
			}
			return Mono.just(filter);
		})
			.flatMap(this::validatePage)
			.flatMap(this::validateSize);
	}
	
	private Mono<PaginationCreditApplicationFilter> validatePage(PaginationCreditApplicationFilter filter) {
		return Mono.just(filter).filter(ft -> ft.getPage() >= 0).switchIfEmpty(Mono.error(new InvalidPaginationFilterException(INVALID_PAGE)));
	}
	
	private Mono<PaginationCreditApplicationFilter> validateSize(PaginationCreditApplicationFilter filter) {
		return Mono.just(filter).filter(ft -> ft.getSize() > 0 && ft.getSize() <= 100).switchIfEmpty(Mono.error(new InvalidPaginationFilterException(INVALID_PAGINATION_FILTER)));
	}
	
}
