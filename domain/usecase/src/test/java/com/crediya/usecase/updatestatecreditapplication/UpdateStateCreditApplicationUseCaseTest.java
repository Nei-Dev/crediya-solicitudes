package com.crediya.usecase.updatestatecreditapplication;

import com.crediya.model.creditapplication.CreditApplication;
import com.crediya.model.creditapplication.StateCreditApplication;
import com.crediya.model.creditapplication.gateways.CreditApplicationRepository;
import com.crediya.model.creditapplication.gateways.MessageService;
import com.crediya.model.exceptions.creditapplication.CreditApplicationNotFoundException;
import com.crediya.model.exceptions.statecreditapplication.InvalidStateCreditApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.crediya.model.constants.CommonCreditApplicationErrorMessage.CREDIT_APPLICATION_NOT_FOUND;
import static com.crediya.model.constants.CommonCreditApplicationErrorMessage.INVALID_ID_CREDIT_APPLICATION;
import static com.crediya.model.constants.UpdateStateCreditApplicationErrorMessage.STATE_CANNOT_BE_MODIFIED;
import static com.crediya.model.constants.UpdateStateCreditApplicationErrorMessage.STATE_INVALID;
import static com.crediya.model.constants.UpdateStateCreditApplicationErrorMessage.STATE_NOT_BLANK;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateStateCreditApplicationUseCaseTest {

    @InjectMocks
    private UpdateStateCreditApplicationUseCase useCase;

    @Mock
    private CreditApplicationRepository repository;

    @Mock
    private MessageService messageService;

    private CreditApplication creditApplication;

    @BeforeEach
    void setUp() {
        creditApplication = CreditApplication.builder()
                .id(1L)
                .state(StateCreditApplication.PENDING)
                .build();
    }

    @Test
    void execute_validUpdate_shouldUpdateStateAndSendMessage() {
        CreditApplication updated = CreditApplication.builder()
                .id(1L)
                .state(StateCreditApplication.APPROVED)
                .build();
        when(repository.findById(1L)).thenReturn(Mono.just(creditApplication));
        when(repository.saveCreditApplication(any(CreditApplication.class))).thenReturn(Mono.just(updated));
        when(messageService.sendChangeStateCreditApplication(any(CreditApplication.class))).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(1L, StateCreditApplication.APPROVED))
                .verifyComplete();
        verify(repository, times(1)).saveCreditApplication(any(CreditApplication.class));
        verify(messageService).sendChangeStateCreditApplication(any(CreditApplication.class));
    }

    @Test
    void execute_nullId_shouldThrowException() {
        StepVerifier.create(useCase.execute(null, StateCreditApplication.APPROVED))
                .expectErrorMatches(e -> e instanceof InvalidStateCreditApplication && e.getMessage().equals(INVALID_ID_CREDIT_APPLICATION))
                .verify();
    }

    @Test
    void execute_invalidId_shouldThrowException() {
        StepVerifier.create(useCase.execute(0L, StateCreditApplication.APPROVED))
                .expectErrorMatches(e -> e instanceof InvalidStateCreditApplication && e.getMessage().equals(INVALID_ID_CREDIT_APPLICATION))
                .verify();
    }

    @Test
    void execute_nullState_shouldThrowException() {
        StepVerifier.create(useCase.execute(1L, null))
                .expectErrorMatches(e -> e instanceof InvalidStateCreditApplication && e.getMessage().equals(STATE_NOT_BLANK))
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
        when(repository.findById(1L)).thenReturn(Mono.empty());
        StepVerifier.create(useCase.execute(1L, StateCreditApplication.REJECTED))
                .expectErrorMatches(e -> e instanceof CreditApplicationNotFoundException && e.getMessage().equals(CREDIT_APPLICATION_NOT_FOUND))
                .verify();
    }

    @Test
    void execute_stateCannotBeModified_shouldThrowException() {
        creditApplication.setState(StateCreditApplication.APPROVED);
        when(repository.findById(1L)).thenReturn(Mono.just(creditApplication));
        StepVerifier.create(useCase.execute(1L, StateCreditApplication.REJECTED))
                .expectErrorMatches(e -> e instanceof InvalidStateCreditApplication && e.getMessage().equals(STATE_CANNOT_BE_MODIFIED))
                .verify();
    }

    @Test
    void execute_errorOnSendMessage_shouldRollbackState() {
        when(repository.findById(1L)).thenReturn(Mono.just(creditApplication));
        when(repository.saveCreditApplication(any(CreditApplication.class))).thenReturn(Mono.just(creditApplication));
        when(messageService.sendChangeStateCreditApplication(any(CreditApplication.class))).thenReturn(Mono.error(new RuntimeException("Error al enviar mensaje")));

        StepVerifier.create(useCase.execute(1L, StateCreditApplication.REJECTED))
                .expectError(RuntimeException.class)
                .verify();
        verify(repository, times(2)).saveCreditApplication(any(CreditApplication.class));
    }
}

