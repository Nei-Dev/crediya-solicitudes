package com.crediya.usecase.calculatecapacity;

import com.crediya.model.creditapplication.CreditApplication;
import com.crediya.model.creditapplication.DebtCapacityCredit;
import com.crediya.model.creditapplication.gateways.CreditApplicationRepository;
import com.crediya.model.creditapplication.gateways.MessageDebtCapacityService;
import com.crediya.model.credittype.CreditType;
import com.crediya.model.credittype.gateways.CreditTypeRepository;
import com.crediya.model.exceptions.TechnicalException;
import com.crediya.model.exceptions.creditapplication.CreditApplicationNotFoundException;
import com.crediya.model.exceptions.creditapplication.InvalidCreditApplicationException;
import com.crediya.model.exceptions.statecreditapplication.InvalidStateCreditApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static com.crediya.model.constants.CalculateCapacityCreditApplicationErrorMessage.FETCHING_DATA_ERROR;
import static com.crediya.model.constants.CalculateCapacityCreditApplicationErrorMessage.STATE_MUST_BE_PENDING;
import static com.crediya.model.constants.CommonCreditApplicationErrorMessage.CREDIT_APPLICATION_NOT_FOUND;
import static com.crediya.model.constants.CommonCreditApplicationErrorMessage.INVALID_ID_CREDIT_APPLICATION;
import static com.crediya.model.creditapplication.StateCreditApplication.APPROVED;
import static com.crediya.model.creditapplication.StateCreditApplication.PENDING;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalculateCapacityUseCaseTest {

    @InjectMocks
    private CalculateCapacityUseCase useCase;

    @Mock
    private CreditApplicationRepository creditApplicationRepository;
    @Mock
    private CreditTypeRepository creditTypeRepository;
    @Mock
    private MessageDebtCapacityService messageDebtCapacityService;

    private CreditApplication pendingApplication;
    private CreditType creditType;

    @BeforeEach
    void setUp() {
        pendingApplication = CreditApplication.builder()
                .id(1L)
                .email("test@example.com")
                .clientSalaryBase(BigDecimal.valueOf(2000))
                .amount(BigDecimal.valueOf(1000))
                .term(12)
                .idCreditType(2L)
                .state(PENDING)
                .build();
        creditType = CreditType.builder()
                .idCreditType(2L)
                .interestRate(BigDecimal.valueOf(0.05))
                .build();
    }

    @Test
    void execute_validPendingApplication_shouldSendDebtCapacityMessage() {
        when(creditApplicationRepository.findById(1L)).thenReturn(Mono.just(pendingApplication));
        when(creditTypeRepository.findById(2L)).thenReturn(Mono.just(creditType));
        when(creditApplicationRepository.findTotalMonthlyDebt("test@example.com")).thenReturn(Mono.just(BigDecimal.valueOf(100)));
        when(messageDebtCapacityService.sendCalculateDebtCapacity(any(DebtCapacityCredit.class))).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(1L))
                .verifyComplete();
        verify(messageDebtCapacityService, times(1)).sendCalculateDebtCapacity(any(DebtCapacityCredit.class));
    }

    @Test
    void execute_nullId_shouldThrowInvalidCreditApplicationException() {
        StepVerifier.create(useCase.execute(null))
                .expectErrorMatches(e -> e instanceof InvalidCreditApplicationException && e.getMessage().equals(INVALID_ID_CREDIT_APPLICATION))
                .verify();
    }

    @Test
    void execute_notFoundApplication_shouldThrowCreditApplicationNotFoundException() {
        when(creditApplicationRepository.findById(99L)).thenReturn(Mono.empty());
        StepVerifier.create(useCase.execute(99L))
                .expectErrorMatches(e -> e instanceof CreditApplicationNotFoundException && e.getMessage().equals(CREDIT_APPLICATION_NOT_FOUND))
                .verify();
    }

    @Test
    void execute_stateNotPending_shouldThrowInvalidStateCreditApplication() {
        CreditApplication approvedApp = CreditApplication.builder()
                .id(2L)
                .state(APPROVED)
                .build();
        when(creditApplicationRepository.findById(2L)).thenReturn(Mono.just(approvedApp));
        StepVerifier.create(useCase.execute(2L))
                .expectErrorMatches(e -> e instanceof InvalidStateCreditApplication && e.getMessage().equals(STATE_MUST_BE_PENDING))
                .verify();
    }

    @Test
    void execute_errorFetchingData_creditTypeRepository_shouldThrowTechnicalException() {
        when(creditApplicationRepository.findById(1L)).thenReturn(Mono.just(pendingApplication));
        when(creditApplicationRepository.findTotalMonthlyDebt(any(String.class))).thenReturn(Mono.just(BigDecimal.ZERO));
        when(creditTypeRepository.findById(2L)).thenReturn(Mono.error(new RuntimeException("db error")));
        StepVerifier.create(useCase.execute(1L))
                .expectErrorMatches(e -> e instanceof TechnicalException && e.getMessage().equals(FETCHING_DATA_ERROR))
                .verify();
    }

    @Test
    void execute_errorFetchingData_totalMonthlyDebt_shouldThrowTechnicalException() {
        when(creditApplicationRepository.findById(1L)).thenReturn(Mono.just(pendingApplication));
        when(creditApplicationRepository.findTotalMonthlyDebt(any(String.class))).thenReturn(Mono.error(new RuntimeException("debt error")));
        when(creditTypeRepository.findById(2L)).thenReturn(Mono.just(creditType));
        StepVerifier.create(useCase.execute(1L))
                .expectErrorMatches(e -> e instanceof TechnicalException && e.getMessage().equals(FETCHING_DATA_ERROR))
                .verify();
    }

    @Test
    void execute_errorOnSendMessage_shouldPropagateError() {
        when(creditApplicationRepository.findById(1L)).thenReturn(Mono.just(pendingApplication));
        when(creditTypeRepository.findById(2L)).thenReturn(Mono.just(creditType));
        when(creditApplicationRepository.findTotalMonthlyDebt("test@example.com")).thenReturn(Mono.just(BigDecimal.valueOf(100)));
        when(messageDebtCapacityService.sendCalculateDebtCapacity(any(DebtCapacityCredit.class)))
                .thenReturn(Mono.error(new RuntimeException("msg error")));
        StepVerifier.create(useCase.execute(1L))
                .expectErrorMatches(e -> e instanceof RuntimeException && e.getMessage().equals("msg error"))
                .verify();
    }
}
