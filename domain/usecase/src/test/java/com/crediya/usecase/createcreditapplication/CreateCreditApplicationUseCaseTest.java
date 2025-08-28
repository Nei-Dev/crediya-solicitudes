package com.crediya.usecase.createcreditapplication;

import com.crediya.model.auth.gateways.AuthService;
import com.crediya.model.creditapplication.CreditApplication;
import com.crediya.model.creditapplication.gateways.CreditApplicationRepository;
import com.crediya.model.creditapplication.ports.ICreateCreditApplicationUseCase;
import com.crediya.model.credittype.gateways.CreditTypeRepository;
import com.crediya.model.exceptions.creditapplication.InvalidCreditApplicationException;
import com.crediya.model.exceptions.credittype.CreditTypeNotFoundException;
import com.crediya.model.exceptions.credittype.InvalidCreditTypeException;
import com.crediya.model.exceptions.user.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class CreateCreditApplicationUseCaseTest {

    @InjectMocks
    private CreateCreditApplicationUseCase useCase;

    @Mock
    private CreditApplicationRepository creditApplicationRepository;

    @Mock
    private CreditTypeRepository creditTypeRepository;

    @Mock
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void execute_validCreditApplication_shouldCreateApplication() {
        CreditApplication creditApplication = new CreditApplication();
        creditApplication.setAmount(BigDecimal.valueOf(1000));
        creditApplication.setTerm(12);
        creditApplication.setIdCreditType(1L);
        creditApplication.setEmail("test@example.com");
        creditApplication.setIdentification("123456789");

        when(authService.findUserByIdentificationNumber(any()))
                .thenReturn(Mono.just(new Object())); // Mock user object
        when(creditTypeRepository.findById(any()))
                .thenReturn(Mono.just(new Object())); // Mock credit type object
        when(creditApplicationRepository.createApplication(any()))
                .thenReturn(Mono.just(creditApplication));

        StepVerifier.create(useCase.execute(creditApplication))
                .expectNext(creditApplication)
                .verifyComplete();
    }

    @Test
    void execute_nullCreditApplication_shouldThrowException() {
        StepVerifier.create(useCase.execute(null))
                .expectError(InvalidCreditApplicationException.class)
                .verify();
    }

    @Test
    void execute_invalidAmount_shouldThrowException() {
        CreditApplication creditApplication = new CreditApplication();
        creditApplication.setAmount(BigDecimal.valueOf(-100));

        StepVerifier.create(useCase.execute(creditApplication))
                .expectError(InvalidCreditApplicationException.class)
                .verify();
    }

    @Test
    void execute_invalidTerm_shouldThrowException() {
        CreditApplication creditApplication = new CreditApplication();
        creditApplication.setAmount(BigDecimal.valueOf(1000));
        creditApplication.setTerm(-1);

        StepVerifier.create(useCase.execute(creditApplication))
                .expectError(InvalidCreditApplicationException.class)
                .verify();
    }

    @Test
    void execute_invalidEmail_shouldThrowException() {
        CreditApplication creditApplication = new CreditApplication();
        creditApplication.setAmount(BigDecimal.valueOf(1000));
        creditApplication.setTerm(12);
        creditApplication.setEmail("invalid-email");

        StepVerifier.create(useCase.execute(creditApplication))
                .expectError(InvalidCreditApplicationException.class)
                .verify();
    }

    @Test
    void execute_userNotFound_shouldThrowException() {
        CreditApplication creditApplication = new CreditApplication();
        creditApplication.setAmount(BigDecimal.valueOf(1000));
        creditApplication.setTerm(12);
        creditApplication.setEmail("test@example.com");
        creditApplication.setIdentification("123456789");

        when(authService.findUserByIdentificationNumber(any()))
                .thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(creditApplication))
                .expectError(UserNotFoundException.class)
                .verify();
    }

    @Test
    void execute_creditTypeNotFound_shouldThrowException() {
        CreditApplication creditApplication = new CreditApplication();
        creditApplication.setAmount(BigDecimal.valueOf(1000));
        creditApplication.setTerm(12);
        creditApplication.setIdCreditType(1L);
        creditApplication.setEmail("test@example.com");
        creditApplication.setIdentification("123456789");

        when(authService.findUserByIdentificationNumber(any()))
                .thenReturn(Mono.just(new Object()));
        when(creditTypeRepository.findById(any()))
                .thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(creditApplication))
                .expectError(CreditTypeNotFoundException.class)
                .verify();
    }
}
