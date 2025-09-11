package com.crediya.usecase.updatestatecreditapplication;

import com.crediya.model.creditapplication.CreditApplication;
import com.crediya.model.creditapplication.StateCreditApplication;
import com.crediya.model.creditapplication.gateways.CreditApplicationRepository;
import com.crediya.model.creditapplication.gateways.MessageChangeStatusService;
import com.crediya.model.credittype.CreditType;
import com.crediya.model.credittype.gateways.CreditTypeRepository;
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

import static com.crediya.model.constants.CommonCreditApplicationErrorMessage.CREDIT_APPLICATION_NOT_FOUND;
import static com.crediya.model.constants.CommonCreditApplicationErrorMessage.INVALID_ID_CREDIT_APPLICATION;
import static com.crediya.model.constants.UpdateStateCreditApplicationErrorMessage.STATE_CANNOT_BE_MODIFIED;
import static com.crediya.model.constants.UpdateStateCreditApplicationErrorMessage.STATE_INVALID;
import static com.crediya.model.creditapplication.StateCreditApplication.APPROVED;
import static com.crediya.model.creditapplication.StateCreditApplication.REJECTED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateStateCreditApplicationUseCaseTest {

    @InjectMocks
    private UpdateStateCreditApplicationUseCase useCase;

    @Mock
    private CreditApplicationRepository creditApplicationRepository;
    
    @Mock
    private CreditTypeRepository creditTypeRepository;

    @Mock
    private MessageChangeStatusService messageChangeStatusService;

    private CreditApplication creditApplication;
    private CreditType creditType;
    
    private CreditApplication pendingApplication () {
        return CreditApplication.builder()
                .id(1L)
                .state(StateCreditApplication.PENDING)
                .idCreditType(1L)
                .build();
    }

    @BeforeEach
    void setUp() {
        creditApplication = pendingApplication();
        creditType = CreditType.builder()
                .idCreditType(1L)
                .interestRate(BigDecimal.ONE)
                .build();
    }

    @Test
    void execute_validUpdate_shouldUpdateStateAndSendMessage() {
        when(creditApplicationRepository.findById(1L)).thenReturn(Mono.just(creditApplication));
        when(creditApplicationRepository.saveCreditApplication(any(CreditApplication.class)))
            .thenAnswer(invocation -> {
                CreditApplication ca = invocation.getArgument(0);
                if (ca.getState() == REJECTED) {
                    return Mono.just(CreditApplication.builder()
                        .id(ca.getId())
                        .state(REJECTED)
                        .idCreditType(ca.getIdCreditType())
                        .build());
                }
                return Mono.just(ca);
            });
        when(creditTypeRepository.findById(creditApplication.getIdCreditType())).thenReturn(Mono.just(creditType));
        when(messageChangeStatusService.sendChangeStateCreditApplication(any(CreditApplication.class), anyList())).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(1L, REJECTED))
                .verifyComplete();
        verify(creditApplicationRepository, times(1)).saveCreditApplication(any(CreditApplication.class));
        verify(messageChangeStatusService).sendChangeStateCreditApplication(any(CreditApplication.class), anyList());
    }

    @Test
    void execute_nullId_shouldThrowException() {
        StepVerifier.create(useCase.execute(null, APPROVED))
                .expectErrorMatches(e -> e instanceof InvalidCreditApplicationException && e.getMessage().equals(INVALID_ID_CREDIT_APPLICATION))
                .verify();
    }

    @Test
    void execute_invalidId_shouldThrowException() {
        StepVerifier.create(useCase.execute(0L, APPROVED))
                .expectErrorMatches(e -> e instanceof InvalidCreditApplicationException && e.getMessage().equals(INVALID_ID_CREDIT_APPLICATION))
                .verify();
    }

    @Test
    void execute_nullState_shouldThrowException() {
        StepVerifier.create(useCase.execute(1L, null))
                .expectErrorMatches(e -> e instanceof InvalidStateCreditApplication && e.getMessage().equals(STATE_INVALID))
                .verify();
    }

    @Test
    void execute_pendingState_shouldThrowException() {
        StepVerifier.create(useCase.execute(1L, StateCreditApplication.PENDING))
                .expectErrorMatches(e -> e instanceof InvalidStateCreditApplication && e.getMessage().equals(STATE_INVALID))
                .verify();
    }

    @Test
    void execute_creditApplicationNotFound_shouldThrowException() {
        when(creditApplicationRepository.findById(1L)).thenReturn(Mono.empty());
        StepVerifier.create(useCase.execute(1L, REJECTED))
                .expectErrorMatches(e -> e instanceof CreditApplicationNotFoundException && e.getMessage().equals(CREDIT_APPLICATION_NOT_FOUND))
                .verify();
    }

    @Test
    void execute_stateCannotBeModified_shouldThrowException() {
        creditApplication.setState(APPROVED);
        when(creditApplicationRepository.findById(1L)).thenReturn(Mono.just(creditApplication));
        StepVerifier.create(useCase.execute(1L, REJECTED))
                .expectErrorMatches(e -> e instanceof InvalidStateCreditApplication && e.getMessage().equals(STATE_CANNOT_BE_MODIFIED))
                .verify();
        
        creditApplication.setState(REJECTED);
        when(creditApplicationRepository.findById(1L)).thenReturn(Mono.just(creditApplication));
        StepVerifier.create(useCase.execute(1L, APPROVED))
                .expectErrorMatches(e -> e instanceof InvalidStateCreditApplication && e.getMessage().equals(STATE_CANNOT_BE_MODIFIED))
                .verify();
    }

    @Test
    void execute_errorOnSendMessage_shouldRollbackState() {
        when(creditApplicationRepository.findById(1L)).thenReturn(Mono.just(creditApplication));
        lenient().when(creditTypeRepository.findById(1L)).thenReturn(Mono.just(creditType));
        lenient().when(messageChangeStatusService.sendChangeStateCreditApplication(any(CreditApplication.class), anyList()))
                .thenReturn(Mono.error(new RuntimeException("Error al enviar mensaje")));
        when(creditApplicationRepository.saveCreditApplication(any(CreditApplication.class)))
                .thenAnswer(invocation -> {
                    CreditApplication ca = invocation.getArgument(0);
                    return Mono.just(
                        CreditApplication.builder()
                            .id(ca.getId())
                            .state(ca.getState())
                            .idCreditType(ca.getIdCreditType())
                            .amount(ca.getAmount())
                            .term(ca.getTerm())
                            .build()
                    );
                });

        StepVerifier.create(useCase.execute(1L, REJECTED))
                .expectError(RuntimeException.class)
                .verify();
        verify(creditApplicationRepository, times(2)).saveCreditApplication(any(CreditApplication.class));
    }
}
