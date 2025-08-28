
package com.crediya.r2dbc;

import com.crediya.model.creditapplication.CreditApplication;
import com.crediya.model.creditapplication.StateCreditApplication;
import com.crediya.r2dbc.creditapplication.CreditApplicationReactiveRepository;
import com.crediya.r2dbc.creditapplication.CreditApplicationRepositoryAdapter;
import com.crediya.r2dbc.entities.CreditApplicationData;
import com.crediya.r2dbc.entities.StateCreditApplicationData;
import com.crediya.r2dbc.exceptions.StateNotFoundException;
import com.crediya.r2dbc.mappers.CreditApplicationEntityMapper;
import com.crediya.r2dbc.statecreditapplication.StateCreditApplicationReactiveRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreditApplicationRepositoryAdapterTest {

    @InjectMocks
    private CreditApplicationRepositoryAdapter repositoryAdapter;

    @Mock
    private CreditApplicationReactiveRepository creditApplicationRepository;

    @Mock
    private StateCreditApplicationReactiveRepository stateRepository;

    @Mock
    private TransactionalOperator transactionalOperator;

    private CreditApplication creditApplication;
    private CreditApplicationData creditApplicationData;
    private StateCreditApplicationData stateCreditApplicationData;

    @BeforeEach
    void setUp() {
        creditApplication = CreditApplication.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(1000.0))
                .term(12)
                .email("test@gmail.com")
                .idCreditType(1L)
                .state(StateCreditApplication.PENDING)
                .build();

        creditApplicationData = CreditApplicationEntityMapper.INSTANCE.toData(creditApplication);
        creditApplicationData.setIdState(1L);

        stateCreditApplicationData = new StateCreditApplicationData();
        stateCreditApplicationData.setId(1L);
        stateCreditApplicationData.setName("PENDING");
    }

    @Test
    void createApplication_Success() {
        when(stateRepository.findByName("PENDING")).thenReturn(Mono.just(stateCreditApplicationData));
        when(creditApplicationRepository.save(any(CreditApplicationData.class))).thenReturn(Mono.just(creditApplicationData));
        when(stateRepository.findById(1L)).thenReturn(Mono.just(stateCreditApplicationData));
        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));


        Mono<CreditApplication> result = repositoryAdapter.createApplication(creditApplication);

        StepVerifier.create(result)
                .expectNextMatches(savedApp -> {
                    assert savedApp.getId().equals(creditApplication.getId());
                    assert savedApp.getState().equals(StateCreditApplication.PENDING);
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void createApplication_StateNotFound() {
        when(stateRepository.findByName("PENDING")).thenReturn(Mono.empty());
        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Mono<CreditApplication> result = repositoryAdapter.createApplication(creditApplication);

        StepVerifier.create(result)
                .expectError(StateNotFoundException.class)
                .verify();
    }
}
