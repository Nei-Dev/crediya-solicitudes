package com.crediya.usecase.getcreditapplication;

import com.crediya.model.PaginationResponse;
import com.crediya.model.creditapplication.CreditApplicationSummary;
import com.crediya.model.creditapplication.PaginationCreditApplicationFilter;
import com.crediya.model.creditapplication.gateways.CreditApplicationRepository;
import com.crediya.model.exceptions.pagination.InvalidPaginationFilterException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;

import static com.crediya.model.constants.ListCreditApplicationErrorMessage.INVALID_PAGE;
import static com.crediya.model.constants.ListCreditApplicationErrorMessage.INVALID_PAGINATION_FILTER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCreditApplicationPaginatedUseCaseTest {

    @InjectMocks
    private GetCreditApplicationPaginatedUseCase useCase;

    @Mock
    private CreditApplicationRepository repository;

    private PaginationCreditApplicationFilter validFilter;
    private PaginationResponse<CreditApplicationSummary> response;

    @BeforeEach
    void setUp() {
        validFilter = PaginationCreditApplicationFilter.builder()
                .page(0)
                .size(10)
                .build();
        response = PaginationResponse.<CreditApplicationSummary>builder()
                .content(Collections.emptyList())
                .page(0)
                .size(10)
                .totalElements(0L)
                .build();
    }

    @Test
    void execute_validFilter_shouldReturnResponse() {
        when(repository.getAllApplications(any(PaginationCreditApplicationFilter.class)))
                .thenReturn(Mono.just(response));

        StepVerifier.create(useCase.execute(validFilter))
                .expectNextMatches(r -> r.getPage() == 0 && r.getSize() == 10)
                .verifyComplete();
    }

    @Test
    void execute_nullFilter_shouldThrowException() {
        StepVerifier.create(useCase.execute(null))
                .expectErrorMatches(e -> e instanceof InvalidPaginationFilterException && e.getMessage().equals(INVALID_PAGINATION_FILTER))
                .verify();
    }

    @Test
    void execute_invalidPage_shouldThrowException() {
        validFilter.setPage(-1);
        StepVerifier.create(useCase.execute(validFilter))
                .expectErrorMatches(e -> e instanceof InvalidPaginationFilterException && e.getMessage().equals(INVALID_PAGE))
                .verify();
    }

    @Test
    void execute_invalidSize_shouldThrowException() {
        validFilter.setSize(0);
        StepVerifier.create(useCase.execute(validFilter))
                .expectErrorMatches(e -> e instanceof InvalidPaginationFilterException && e.getMessage().equals(INVALID_PAGINATION_FILTER))
                .verify();

        validFilter.setSize(101);
        StepVerifier.create(useCase.execute(validFilter))
                .expectErrorMatches(e -> e instanceof InvalidPaginationFilterException && e.getMessage().equals(INVALID_PAGINATION_FILTER))
                .verify();
    }
}
