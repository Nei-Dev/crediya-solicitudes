package com.crediya.usecase.createcreditapplication;

import com.crediya.model.creditapplication.CreditApplication;
import com.crediya.model.creditapplication.gateways.CreditApplicationRepository;
import com.crediya.model.credittype.CreditType;
import com.crediya.model.credittype.gateways.CreditTypeRepository;
import com.crediya.model.exceptions.creditapplication.InvalidCreditApplicationException;
import com.crediya.model.exceptions.credittype.CreditTypeNotFoundException;
import com.crediya.model.exceptions.credittype.InvalidCreditTypeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Objects;

import static com.crediya.model.constants.ErrorMessage.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateCreditApplicationUseCaseTest {

    @InjectMocks
    private CreateCreditApplicationUseCase useCase;

    @Mock
    private CreditApplicationRepository creditApplicationRepository;

    @Mock
    private CreditTypeRepository creditTypeRepository;
    
    private CreditApplication creditApplication;
    
    private CreditType creditTypeRegistered;
    
    @BeforeEach
    void setUp() {
        creditApplication = CreditApplication
            .builder()
            .amount(BigDecimal.valueOf(1000))
            .term(12)
            .idCreditType(1L)
            .email("test@example.com")
            .identification("123456789")
            .build();
        
        creditTypeRegistered = CreditType.builder()
            .idCreditType(1L)
            .name("Personal Loan")
            .minimumAmount(BigDecimal.valueOf(500))
            .maximumAmount(BigDecimal.valueOf(5000))
            .build();
    }
    
    @Test
    void execute_validCreditApplication_shouldCreateApplication() {
        CreditApplication creditApplicationSaved = CreditApplication
            .builder()
            .id(1L)
            .amount(BigDecimal.valueOf(1000))
            .term(12)
            .idCreditType(1L)
            .email("test@example.com")
            .build();

        when(creditTypeRepository.findById(any(Long.class)))
                .thenReturn(Mono.just(creditTypeRegistered));
        when(creditApplicationRepository.createApplication(any(CreditApplication.class)))
                .thenReturn(Mono.just(creditApplicationSaved));

        StepVerifier.create(useCase.execute(creditApplication))
                .expectNextMatches(result -> Objects.nonNull(result.getId()) && result.getId().equals(creditApplicationSaved.getId())
                    && result.getAmount().equals(creditApplicationSaved.getAmount())
                    && result.getTerm().equals(creditApplicationSaved.getTerm())
                    && result.getIdCreditType().equals(creditApplicationSaved.getIdCreditType())
                    && result.getEmail().equals(creditApplicationSaved.getEmail()
                ))
                .verifyComplete();
    }

    @Test
    void execute_nullCreditApplication_shouldThrowException() {
        StepVerifier.create(useCase.execute(null))
            .expectError(InvalidCreditApplicationException.class)
            .verify();
    }

    @Test
    void execute_creditTypeNotFound_shouldThrowException() {
        creditApplication.setIdCreditType(10L);

        when(creditTypeRepository.findById(creditApplication.getIdCreditType()))
            .thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(creditApplication))
            .expectError(CreditTypeNotFoundException.class)
            .verify();
    }
    
    @Test
    void execute_invalidAmount_shouldThrowException() {
        creditApplication.setAmount(BigDecimal.valueOf(-1000));
        StepVerifier.create(useCase.execute(creditApplication))
            .expectErrorMatches(e -> e instanceof InvalidCreditApplicationException
                && e.getMessage().equals(INVALID_AMOUNT_REQUESTED))
            .verify();
        
        creditApplication.setAmount(null);
        StepVerifier.create(useCase.execute(creditApplication))
            .expectErrorMatches(e -> e instanceof InvalidCreditApplicationException
                && e.getMessage().equals(INVALID_AMOUNT_REQUESTED))
            .verify();
    }
    
    @Test
    void execute_invalidTerm_shouldThrowException() {
        creditApplication.setTerm(0);
        StepVerifier.create(useCase.execute(creditApplication))
            .expectErrorMatches(e -> e instanceof InvalidCreditApplicationException
                && e.getMessage().equals(INVALID_TERM_IN_MONTHS))
            .verify();
        
        creditApplication.setTerm(null);
        StepVerifier.create(useCase.execute(creditApplication))
            .expectErrorMatches(e -> e instanceof InvalidCreditApplicationException
                && e.getMessage().equals(INVALID_TERM_IN_MONTHS))
            .verify();
    }
    
    @Test
    void execute_invalidIdCreditTypeOrEmail_shouldThrowException() {
        creditApplication.setIdCreditType(null);
        StepVerifier.create(useCase.execute(creditApplication))
            .expectErrorMatches(e -> e instanceof InvalidCreditTypeException && e.getMessage().equals(INVALID_ID_CREDIT_TYPE))
            .verify();
        
        creditApplication.setIdCreditType(0L);
        StepVerifier.create(useCase.execute(creditApplication))
            .expectErrorMatches(e -> e instanceof InvalidCreditTypeException && e.getMessage().equals(INVALID_ID_CREDIT_TYPE))
            .verify();
    }
    
    @Test
    void execute_invalidEmail_shouldThrowException() {
        creditApplication.setEmail(null);
        StepVerifier.create(useCase.execute(creditApplication))
            .expectErrorMatches(e -> e instanceof InvalidCreditApplicationException && e.getMessage().equals(INVALID_EMAIL))
            .verify();
        
        creditApplication.setEmail("");
        StepVerifier.create(useCase.execute(creditApplication))
            .expectErrorMatches(e -> e instanceof InvalidCreditApplicationException && e.getMessage().equals(INVALID_EMAIL))
            .verify();
        
        creditApplication.setEmail("invalid-email");
        StepVerifier.create(useCase.execute(creditApplication))
            .expectErrorMatches(e -> e instanceof InvalidCreditApplicationException && e.getMessage().equals(INVALID_EMAIL))
            .verify();
    }
    
    @Test
    void execute_outOfRangeAmount_shouldThrowException() {
        creditApplication.setAmount(BigDecimal.valueOf(10000));
        when(creditTypeRepository.findById(creditApplication.getIdCreditType()))
            .thenReturn(Mono.just(creditTypeRegistered));
        
        StepVerifier.create(useCase.execute(creditApplication))
            .expectErrorMatches(e -> e instanceof InvalidCreditApplicationException && e.getMessage().equals(AMOUNT_REQUESTED_OUT_OF_RANGE))
            .verify();
        
        creditApplication.setAmount(BigDecimal.valueOf(100));
        when(creditTypeRepository.findById(creditApplication.getIdCreditType()))
            .thenReturn(Mono.just(creditTypeRegistered));
        
        StepVerifier.create(useCase.execute(creditApplication))
            .expectErrorMatches(e -> e instanceof InvalidCreditApplicationException && e.getMessage().equals(AMOUNT_REQUESTED_OUT_OF_RANGE))
            .verify();
    }
    
    @Test
    void execute_invalidIdentification_shouldThrowException() {
        creditApplication.setIdentification(null);
        StepVerifier.create(useCase.execute(creditApplication))
            .expectErrorMatches(e -> e instanceof InvalidCreditApplicationException && e.getMessage().equals(INVALID_IDENTIFICATION))
            .verify();
        
        creditApplication.setIdentification("");
        StepVerifier.create(useCase.execute(creditApplication))
            .expectErrorMatches(e -> e instanceof InvalidCreditApplicationException && e.getMessage().equals(INVALID_IDENTIFICATION))
            .verify();
        
        creditApplication.setIdentification("invalid-id!");
        StepVerifier.create(useCase.execute(creditApplication))
            .expectErrorMatches(e -> e instanceof InvalidCreditApplicationException && e.getMessage().equals(INVALID_IDENTIFICATION))
            .verify();
    }
}
